package com.dyl.openbanking_mcp.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.dyl.openbanking_mcp.config.OpenBankingProperties;
import com.dyl.openbanking_mcp.token.TokenService;
import com.sun.net.httpserver.HttpServer;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenBankingClientTests {

	// Feature: openbanking-mcp-tools, Property 2: Bearer token header formatting

	/**
	 * Validates: Requirements 2.2, 9.3
	 *
	 * For any non-blank token string returned by the TokenService, the HTTP request
	 * to the Open Banking API SHALL contain an Authorization header with the exact
	 * value "Bearer " + token (single space separator, no trailing whitespace).
	 */
	@Property
	void bearerTokenHeaderFormatting(@ForAll("validTokens") String token) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		String[] capturedAuthHeader = new String[1];

		server.createContext("/test", exchange -> {
			capturedAuthHeader[0] = exchange.getRequestHeaders().getFirst("Authorization");
			byte[] response = "{}".getBytes();
			exchange.sendResponseHeaders(200, response.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(response);
			}
		});
		server.start();

		try {
			OpenBankingProperties properties = new OpenBankingProperties();
			properties.setBaseUrl("http://localhost:" + port);
			properties.setConnectTimeout(5);
			properties.setRequestTimeout(5);

			TokenService tokenService = () -> token;

			OpenBankingClient client = new OpenBankingClient(tokenService, properties);
			client.get("/test");

			assertThat(capturedAuthHeader[0]).isEqualTo("Bearer " + token);
		} finally {
			server.stop(0);
		}
	}

	@Provide
	Arbitrary<String> validTokens() {
		// Generate non-blank strings using printable ASCII characters (valid for HTTP headers)
		return Arbitraries.strings()
				.withCharRange('!', '~')  // printable ASCII excluding space
				.ofMinLength(1)
				.ofMaxLength(200);
	}

	// Feature: openbanking-mcp-tools, Property 8: Unique x-fapi-interaction-id per request

	/**
	 * Validates: Requirements 9.1
	 *
	 * For any sequence of N HTTP requests sent by the OpenBankingClient (where N >= 2),
	 * every x-fapi-interaction-id header value SHALL be a valid RFC 4122 UUID v4 string,
	 * and all N values SHALL be distinct from each other.
	 */
	@Property(tries = 100)
	void allFapiInteractionIdsAreUniqueUuidV4(@ForAll @IntRange(min = 2, max = 10) int requestCount) throws IOException {
		List<String> capturedInteractionIds = new ArrayList<>();

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			String interactionId = exchange.getRequestHeaders().getFirst("x-fapi-interaction-id");
			synchronized (capturedInteractionIds) {
				capturedInteractionIds.add(interactionId);
			}
			byte[] response = "{}".getBytes();
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.getResponseBody().close();
		});
		server.start();

		try {
			OpenBankingProperties properties = new OpenBankingProperties();
			properties.setBaseUrl("http://localhost:" + port);
			properties.setConnectTimeout(5);
			properties.setRequestTimeout(5);

			TokenService mockTokenService = () -> "test-token";

			OpenBankingClient client = new OpenBankingClient(mockTokenService, properties);

			for (int i = 0; i < requestCount; i++) {
				client.get("/test");
			}

			assertThat(capturedInteractionIds).hasSize(requestCount);

			// Verify each is a valid UUID v4
			for (String id : capturedInteractionIds) {
				assertThat(id).isNotNull().isNotBlank();
				// Parse as UUID to verify format
				UUID parsed = UUID.fromString(id);
				// UUID v4 has version bits set: version == 4
				assertThat(parsed.version()).isEqualTo(4);
			}

			// Verify all are distinct
			Set<String> uniqueIds = new HashSet<>(capturedInteractionIds);
			assertThat(uniqueIds).hasSameSizeAs(capturedInteractionIds);
		} finally {
			server.stop(0);
		}
	}

	// Feature: openbanking-mcp-tools, Property 3: Blank or null token rejection

	/**
	 * Validates: Requirements 2.3
	 *
	 * For any token value that is null or composed entirely of whitespace characters,
	 * the tool SHALL return an error response indicating authentication is unavailable
	 * and SHALL NOT send any HTTP request to the Open Banking API.
	 */
	@Property
	void blankOrNullTokenThrowsExceptionBeforeHttpRequest(@ForAll("blankOrNullTokens") String token) {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> token;
		OpenBankingClient client = new OpenBankingClient(tokenService, properties);

		assertThatThrownBy(() -> client.get("/accounts"))
				.isInstanceOf(OpenBankingClientException.class)
				.hasMessageContaining("Authentication unavailable");
	}

	@Provide
	Arbitrary<String> blankOrNullTokens() {
		return Arbitraries.of(
				null,
				"",
				" ",
				"\t",
				"\n",
				"\r",
				"   ",
				"\t\t",
				"\n\n",
				" \t \n ",
				"\r\n",
				"  \t  \n  "
		);
	}

	// Feature: openbanking-mcp-tools, Property 9: Response interaction ID propagation

	/**
	 * Validates: Requirements 9.4
	 *
	 * For any HTTP response from the Open Banking API that includes an
	 * x-fapi-interaction-id header, the tool result returned to the MCP caller
	 * SHALL contain that header's value.
	 */
	@Property
	void responseInteractionIdPropagation(@ForAll("responseInteractionIds") String responseInteractionId) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/test", exchange -> {
			exchange.getResponseHeaders().add("x-fapi-interaction-id", responseInteractionId);
			byte[] response = "{\"data\":\"test\"}".getBytes();
			exchange.sendResponseHeaders(200, response.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(response);
			}
		});
		server.start();

		try {
			OpenBankingProperties properties = new OpenBankingProperties();
			properties.setBaseUrl("http://localhost:" + port);
			properties.setConnectTimeout(5);
			properties.setRequestTimeout(5);

			TokenService tokenService = () -> "test-token";

			OpenBankingClient client = new OpenBankingClient(tokenService, properties);
			String result = client.get("/test");

			assertThat(result).contains(responseInteractionId);
			assertThat(result).contains("[x-fapi-interaction-id: " + responseInteractionId + "]");
		} finally {
			server.stop(0);
		}
	}
	
	@Provide
	Arbitrary<String> responseInteractionIds() {
		// Generate random UUID strings to simulate response interaction IDs
		return Arbitraries.randomValue(random -> UUID.randomUUID().toString());
	}
}
