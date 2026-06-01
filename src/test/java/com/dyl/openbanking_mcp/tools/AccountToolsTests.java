package com.dyl.openbanking_mcp.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.client.OpenBankingClientException;
import com.dyl.openbanking_mcp.config.OpenBankingProperties;
import com.dyl.openbanking_mcp.model.OBReadAccount6;
import com.dyl.openbanking_mcp.token.TokenService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountToolsTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	// Feature: openbanking-mcp-tools, Property 5: Error response formatting

	/**
	 * Validates: Requirements 3.4, 4.4, 5.4, 6.4, 7.6
	 *
	 * For any tool invocation where the Open Banking API returns a non-2xx HTTP status code,
	 * the tool SHALL throw a McpToolError that contains both the numeric HTTP status
	 * code and the error details from the response body.
	 */
	@Property(tries = 100)
	void errorResponseContainsStatusCodeAndBody(
			@ForAll("errorStatusCodes") int statusCode,
			@ForAll("errorBodies") String errorBody) throws IOException {

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/accounts", exchange -> {
			byte[] response = errorBody.getBytes();
			exchange.sendResponseHeaders(statusCode, response.length);
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

			TokenService mockTokenService = () -> "test-token";

			OpenBankingClient client = new OpenBankingClient(mockTokenService, properties);
			AccountTools accountTools = new AccountTools(client, objectMapper);

			assertThatThrownBy(() -> accountTools.listAccounts())
					.isInstanceOf(McpToolError.class)
					.hasMessageContaining(String.valueOf(statusCode))
					.hasMessageContaining(errorBody);
		} finally {
			server.stop(0);
		}
	}

	@Provide
	Arbitrary<Integer> errorStatusCodes() {
		return Arbitraries.of(400, 401, 403, 404, 405, 406, 429, 500, 502, 503);
	}

	@Provide
	Arbitrary<String> errorBodies() {
		return Arbitraries.of(
				"{\"Code\":\"400\",\"Message\":\"Bad Request\"}",
				"{\"Code\":\"401\",\"Message\":\"Unauthorized\"}",
				"{\"Code\":\"403\",\"Message\":\"Forbidden\"}",
				"{\"Code\":\"404\",\"Message\":\"Not Found\"}",
				"{\"Code\":\"500\",\"Message\":\"Internal Server Error\"}",
				"Service temporarily unavailable",
				"{\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Resource.NotFound\"}]}",
				"Rate limit exceeded",
				"{\"error\":\"invalid_token\",\"error_description\":\"Token expired\"}",
				"Bad Gateway"
		);
	}

	// Feature: openbanking-mcp-tools, Property 4: Successful response pass-through

	/**
	 * Validates: Requirements 3.3, 4.3, 5.3, 6.3, 7.5
	 *
	 * For any tool invocation where the Open Banking API returns HTTP 200 with a valid JSON
	 * response body, the tool SHALL return a typed OBReadAccount6 object.
	 */
	@Property(tries = 100)
	void successfulResponseReturnsTypedObject(@ForAll("accountJsonBodies") String jsonBody) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/accounts", exchange -> {
			byte[] response = jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
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

			TokenService mockTokenService = () -> "test-token";

			OpenBankingClient client = new OpenBankingClient(mockTokenService, properties);
			AccountTools accountTools = new AccountTools(client, objectMapper);

			OBReadAccount6 result = accountTools.listAccounts();

			assertThat(result).isInstanceOf(OBReadAccount6.class);
		} finally {
			server.stop(0);
		}
	}

	@Provide
	Arbitrary<String> accountJsonBodies() {
		return Arbitraries.strings()
				.withCharRange('a', 'z')
				.ofMinLength(1)
				.ofMaxLength(50)
				.map(value -> "{\"Data\":{\"Account\":[{\"AccountId\":\"" + value + "\"}]}}");
	}

	// --- Example-based unit tests ---

	@Test
	void getAccount_accountIdExactly40Chars_shouldMakeHttpCall() throws IOException {
		String accountId40 = "x".repeat(40);
		String responseJson = "{\"Data\":{\"Account\":[{\"AccountId\":\"" + accountId40 + "\"}]}}";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			String path = exchange.getRequestURI().getPath();
			assertThat(path).isEqualTo("/accounts/" + accountId40);
			byte[] response = responseJson.getBytes();
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
			AccountTools accountTools = new AccountTools(client, objectMapper);

			OBReadAccount6 result = accountTools.getAccount(accountId40);

			assertThat(result).isInstanceOf(OBReadAccount6.class);
			assertThat(result.data().account().get(0).accountId()).isEqualTo(accountId40);
		} finally {
			server.stop(0);
		}
	}

	@Test
	void getAccount_accountIdWithSpecialCharacters_shouldMakeHttpCall() throws IOException {
		String accountIdSpecial = "acc-789_test.special!@#$";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = "{\"Data\":{\"Account\":[{\"AccountId\":\"special\"}]}}".getBytes();
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
			AccountTools accountTools = new AccountTools(client, objectMapper);

			OBReadAccount6 result = accountTools.getAccount(accountIdSpecial);

			assertThat(result).isInstanceOf(OBReadAccount6.class);
			assertThat(result.data().account().get(0).accountId()).isEqualTo("special");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void getAccount_emptyResponseBody_shouldThrowParseError() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			exchange.sendResponseHeaders(200, 0);
			exchange.getResponseBody().close();
		});
		server.start();

		try {
			OpenBankingProperties properties = new OpenBankingProperties();
			properties.setBaseUrl("http://localhost:" + port);
			properties.setConnectTimeout(5);
			properties.setRequestTimeout(5);

			TokenService tokenService = () -> "test-token";
			OpenBankingClient client = new OpenBankingClient(tokenService, properties);
			AccountTools accountTools = new AccountTools(client, objectMapper);

			assertThatThrownBy(() -> accountTools.getAccount("valid-account-id"))
					.isInstanceOf(McpToolError.class)
					.hasMessageContaining("Failed to parse API response");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void getAccount_tokenException_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService failingTokenService = () -> {
			throw new RuntimeException("OAuth2 token endpoint down");
		};

		OpenBankingClient client = new OpenBankingClient(failingTokenService, properties);
		AccountTools accountTools = new AccountTools(client, objectMapper);

		assertThatThrownBy(() -> accountTools.getAccount("valid-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("Authentication unavailable");
	}

	@Test
	void listAccounts_tokenException_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService failingTokenService = () -> {
			throw new RuntimeException("Token service crashed");
		};

		OpenBankingClient client = new OpenBankingClient(failingTokenService, properties);
		AccountTools accountTools = new AccountTools(client, objectMapper);

		assertThatThrownBy(() -> accountTools.listAccounts())
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("Authentication unavailable");
	}

	@Test
	void getAccount_unexpectedRuntimeException_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> "test-token";
		OpenBankingClient client = new OpenBankingClient(tokenService, properties) {
			@Override
			public String get(String path) {
				throw new RuntimeException("Unexpected NullPointerException");
			}
		};

		AccountTools accountTools = new AccountTools(client, objectMapper);

		assertThatThrownBy(() -> accountTools.getAccount("valid-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("An unexpected error occurred - ")
				.hasMessageContaining("Unexpected NullPointerException");
	}

	@Test
	void listAccounts_unexpectedRuntimeException_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> "test-token";
		OpenBankingClient client = new OpenBankingClient(tokenService, properties) {
			@Override
			public String get(String path) {
				throw new RuntimeException("Serialization error");
			}
		};

		AccountTools accountTools = new AccountTools(client, objectMapper);

		assertThatThrownBy(() -> accountTools.listAccounts())
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("An unexpected error occurred - ")
				.hasMessageContaining("Serialization error");
	}

	@Test
	void listAccounts_responseWithFapiInteractionId_shouldStripAndDeserialize() throws IOException {
		String jsonBody = "{\"Data\":{\"Account\":[{\"AccountId\":\"acc-001\",\"Currency\":\"GBP\"}]}}";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/accounts", exchange -> {
			byte[] response = jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("x-fapi-interaction-id", "d5b8e2f0-3c4a-4e6b-9a1d-7f8e2c3b4a5d");
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
			AccountTools accountTools = new AccountTools(client, objectMapper);

			OBReadAccount6 result = accountTools.listAccounts();

			assertThat(result).isInstanceOf(OBReadAccount6.class);
			assertThat(result.data().account().get(0).accountId()).isEqualTo("acc-001");
			assertThat(result.data().account().get(0).currency()).isEqualTo("GBP");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void getAccount_responseWithFapiInteractionId_shouldStripAndDeserialize() throws IOException {
		String accountId = "acc-123";
		String jsonBody = "{\"Data\":{\"Account\":[{\"AccountId\":\"" + accountId + "\",\"Nickname\":\"My Account\"}]}}";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("x-fapi-interaction-id", "a1b2c3d4-e5f6-7890-abcd-ef1234567890");
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
			AccountTools accountTools = new AccountTools(client, objectMapper);

			OBReadAccount6 result = accountTools.getAccount(accountId);

			assertThat(result).isInstanceOf(OBReadAccount6.class);
			assertThat(result.data().account().get(0).accountId()).isEqualTo(accountId);
			assertThat(result.data().account().get(0).nickname()).isEqualTo("My Account");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void getAccount_openBankingClientException_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> "test-token";
		OpenBankingClient client = new OpenBankingClient(tokenService, properties) {
			@Override
			public String get(String path) {
				throw new OpenBankingClientException("Unable to connect to Open Banking API - connection timed out after 30 seconds");
			}
		};

		AccountTools accountTools = new AccountTools(client, objectMapper);

		assertThatThrownBy(() -> accountTools.getAccount("valid-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessage("Unable to connect to Open Banking API - connection timed out after 30 seconds");
	}
}
