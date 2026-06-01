package com.dyl.openbanking_mcp.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.client.OpenBankingClientException;
import com.dyl.openbanking_mcp.config.OpenBankingProperties;
import com.dyl.openbanking_mcp.model.OBReadProduct2;
import com.dyl.openbanking_mcp.token.TokenService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Example-based unit tests for ProductTools.
 * Validates: Requirements 3.3, 5.1, 5.4, 6.1, 6.2, 8.1, 8.2, 8.3
 */
class ProductToolsTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Test
	void accountIdExactly40Chars_shouldMakeHttpCall() throws IOException {
		String accountId40 = "b".repeat(40);

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			String path = exchange.getRequestURI().getPath();
			assertThat(path).isEqualTo("/accounts/" + accountId40 + "/product");
			byte[] response = "{\"Data\":{\"Product\":[]}}".getBytes();
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
			ProductTools productTools = new ProductTools(client, objectMapper);

			OBReadProduct2 result = productTools.getProduct(accountId40);

			assertThat(result).isInstanceOf(OBReadProduct2.class);
		} finally {
			server.stop(0);
		}
	}

	@Test
	void accountIdWithSpecialCharacters_shouldMakeHttpCall() throws IOException {
		String accountIdSpecial = "acc-456_prod.test!@";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = "{\"Data\":{\"Product\":[{\"ProductName\":\"Savings\"}]}}".getBytes();
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
			ProductTools productTools = new ProductTools(client, objectMapper);

			OBReadProduct2 result = productTools.getProduct(accountIdSpecial);

			assertThat(result).isInstanceOf(OBReadProduct2.class);
			assertThat(result.data().product()).hasSize(1);
			assertThat(result.data().product().get(0).productName()).isEqualTo("Savings");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void emptyResponseBody_shouldThrowMcpToolError() throws IOException {
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
			ProductTools productTools = new ProductTools(client, objectMapper);

			assertThatThrownBy(() -> productTools.getProduct("valid-account-id"))
					.isInstanceOf(McpToolError.class)
					.hasMessageContaining("Failed to parse API response");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void tokenException_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService failingTokenService = () -> {
			throw new RuntimeException("Token endpoint unreachable");
		};

		OpenBankingClient client = new OpenBankingClient(failingTokenService, properties);
		ProductTools productTools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> productTools.getProduct("valid-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("Authentication unavailable");
	}

	@Test
	void unexpectedRuntimeException_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> "test-token";
		OpenBankingClient client = new OpenBankingClient(tokenService, properties) {
			@Override
			public String get(String path) {
				throw new RuntimeException("Unexpected failure in product call");
			}
		};

		ProductTools productTools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> productTools.getProduct("valid-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("An unexpected error occurred - ")
				.hasMessageContaining("Unexpected failure in product call");
	}

	@Test
	void openBankingClientException_shouldThrowMcpToolError() {
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

		ProductTools productTools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> productTools.getProduct("valid-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessage("Unable to connect to Open Banking API - connection timed out after 30 seconds");
	}

	@Test
	void accountIdOver40Chars_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> {
			throw new AssertionError("Should not call token service for invalid input");
		};

		OpenBankingClient client = new OpenBankingClient(tokenService, properties);
		ProductTools productTools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> productTools.getProduct("c".repeat(41)))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("1 and 40 characters");
	}

	@Test
	void nullAccountId_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> {
			throw new AssertionError("Should not call token service for null input");
		};

		OpenBankingClient client = new OpenBankingClient(tokenService, properties);
		ProductTools productTools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> productTools.getProduct(null))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("1 and 40 characters");
	}

	@Test
	void blankAccountId_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> {
			throw new AssertionError("Should not call token service for blank input");
		};

		OpenBankingClient client = new OpenBankingClient(tokenService, properties);
		ProductTools productTools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> productTools.getProduct("   "))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("1 and 40 characters");
	}

	@Test
	void successfulResponse_shouldDeserializeIntoOBReadProduct2() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			String json = "{\"Data\":{\"Product\":[{\"AccountId\":\"acc-123\",\"ProductName\":\"Current Account\",\"ProductType\":\"PersonalCurrentAccount\"}]},\"Links\":{\"Self\":\"https://api.example.com/accounts/acc-123/product\"},\"Meta\":{\"TotalPages\":1}}";
			byte[] response = json.getBytes();
			exchange.getResponseHeaders().add("x-fapi-interaction-id", "test-interaction-id");
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
			ProductTools productTools = new ProductTools(client, objectMapper);

			OBReadProduct2 result = productTools.getProduct("acc-123");

			assertThat(result).isInstanceOf(OBReadProduct2.class);
			assertThat(result.data().product()).hasSize(1);
			assertThat(result.data().product().get(0).accountId()).isEqualTo("acc-123");
			assertThat(result.data().product().get(0).productName()).isEqualTo("Current Account");
			assertThat(result.data().product().get(0).productType()).isEqualTo("PersonalCurrentAccount");
			assertThat(result.links().self()).isEqualTo("https://api.example.com/accounts/acc-123/product");
			assertThat(result.meta().totalPages()).isEqualTo(1);
		} finally {
			server.stop(0);
		}
	}

	@Test
	void responseWithFapiInteractionId_shouldStripMetadataBeforeDeserialization() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			String json = "{\"Data\":{\"Product\":[]}}";
			byte[] response = json.getBytes();
			exchange.getResponseHeaders().add("x-fapi-interaction-id", "abc-123");
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
			ProductTools productTools = new ProductTools(client, objectMapper);

			OBReadProduct2 result = productTools.getProduct("acc-123");

			// Should successfully deserialize despite x-fapi-interaction-id being appended by client
			assertThat(result).isInstanceOf(OBReadProduct2.class);
		} finally {
			server.stop(0);
		}
	}

	@Test
	void invalidJsonResponse_shouldThrowMcpToolError() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> "test-token";
		OpenBankingClient client = new OpenBankingClient(tokenService, properties) {
			@Override
			public String get(String path) {
				return "not valid json {{{";
			}
		};

		ProductTools productTools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> productTools.getProduct("acc-123"))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("Failed to parse API response");
	}
}
