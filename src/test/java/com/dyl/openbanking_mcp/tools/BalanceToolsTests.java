package com.dyl.openbanking_mcp.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.client.OpenBankingClientException;
import com.dyl.openbanking_mcp.config.OpenBankingProperties;
import com.dyl.openbanking_mcp.model.OBReadBalance1;
import com.dyl.openbanking_mcp.token.TokenService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Example-based unit tests for BalanceTools.
 * Validates: Requirements 2.4, 5.1, 5.4
 */
class BalanceToolsTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Test
	void successfulResponse_shouldDeserializeIntoOBReadBalance1WithFullFieldVerification() throws IOException {
		String responseJson = """
				{"Data":{"Balance":[{"AccountId":"ACC-12345","CreditDebitIndicator":"Credit","Type":"InterimAvailable","DateTime":"2024-01-15T10:30:00Z","Amount":{"Amount":"1250.75","Currency":"GBP","SubType":"BaseCurrency"},"CreditLine":[{"Included":true,"Type":"Pre-Agreed","Amount":{"Amount":"500.00","Currency":"GBP"}}]}]},"Links":{"Self":"https://api.example.com/balances"},"Meta":{"TotalPages":1}}""";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = responseJson.getBytes(StandardCharsets.UTF_8);
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
			BalanceTools balanceTools = new BalanceTools(client, objectMapper);

			OBReadBalance1 result = balanceTools.getBalances("ACC-12345");

			assertThat(result).isInstanceOf(OBReadBalance1.class);
			assertThat(result.data().balance()).hasSize(1);
			assertThat(result.data().balance().get(0).accountId()).isEqualTo("ACC-12345");
			assertThat(result.data().balance().get(0).creditDebitIndicator()).isEqualTo("Credit");
			assertThat(result.data().balance().get(0).type()).isEqualTo("InterimAvailable");
			assertThat(result.data().balance().get(0).dateTime()).isEqualTo("2024-01-15T10:30:00Z");
			assertThat(result.data().balance().get(0).amount().amount()).isEqualTo("1250.75");
			assertThat(result.data().balance().get(0).amount().currency()).isEqualTo("GBP");
			assertThat(result.data().balance().get(0).amount().subType()).isEqualTo("BaseCurrency");
			assertThat(result.data().balance().get(0).creditLine()).hasSize(1);
			assertThat(result.data().balance().get(0).creditLine().get(0).included()).isTrue();
			assertThat(result.data().balance().get(0).creditLine().get(0).type()).isEqualTo("Pre-Agreed");
			assertThat(result.data().balance().get(0).creditLine().get(0).amount().amount()).isEqualTo("500.00");
			assertThat(result.data().balance().get(0).creditLine().get(0).amount().currency()).isEqualTo("GBP");
			assertThat(result.links().self()).isEqualTo("https://api.example.com/balances");
			assertThat(result.meta().totalPages()).isEqualTo(1);
		} finally {
			server.stop(0);
		}
	}

	@Test
	void responseWithFapiInteractionId_shouldDeserializeCorrectly() throws IOException {
		String jsonBody = "{\"Data\":{\"Balance\":[{\"AccountId\":\"ACC-999\",\"CreditDebitIndicator\":\"Debit\",\"Type\":\"ClosingAvailable\",\"DateTime\":\"2024-03-01T08:00:00Z\",\"Amount\":{\"Amount\":\"320.50\",\"Currency\":\"EUR\"}}]}}";
		String responseWithFapi = jsonBody + "\n[x-fapi-interaction-id: abc-123-def-456]";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = responseWithFapi.getBytes(StandardCharsets.UTF_8);
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
			BalanceTools balanceTools = new BalanceTools(client, objectMapper);

			OBReadBalance1 result = balanceTools.getBalances("ACC-999");

			assertThat(result).isInstanceOf(OBReadBalance1.class);
			assertThat(result.data().balance()).hasSize(1);
			assertThat(result.data().balance().get(0).accountId()).isEqualTo("ACC-999");
			assertThat(result.data().balance().get(0).creditDebitIndicator()).isEqualTo("Debit");
			assertThat(result.data().balance().get(0).type()).isEqualTo("ClosingAvailable");
			assertThat(result.data().balance().get(0).amount().amount()).isEqualTo("320.50");
			assertThat(result.data().balance().get(0).amount().currency()).isEqualTo("EUR");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void invalidJsonResponse_shouldThrowMcpToolError() throws IOException {
		String invalidJson = "{not valid json at all!!!";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = invalidJson.getBytes(StandardCharsets.UTF_8);
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
			BalanceTools balanceTools = new BalanceTools(client, objectMapper);

			assertThatThrownBy(() -> balanceTools.getBalances("valid-account-id"))
					.isInstanceOf(McpToolError.class)
					.hasMessageContaining("Failed to parse API response");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void accountIdExactly40Chars_shouldMakeHttpCall() throws IOException {
		String accountId40 = "a".repeat(40);

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			String path = exchange.getRequestURI().getPath();
			assertThat(path).isEqualTo("/accounts/" + accountId40 + "/balances");
			byte[] response = "{\"Data\":{\"Balance\":[]}}".getBytes();
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
			BalanceTools balanceTools = new BalanceTools(client, objectMapper);

			OBReadBalance1 result = balanceTools.getBalances(accountId40);

			assertThat(result).isInstanceOf(OBReadBalance1.class);
		} finally {
			server.stop(0);
		}
	}

	@Test
	void accountIdWithSpecialCharacters_shouldMakeHttpCall() throws IOException {
		String accountIdSpecial = "acc-123_test.special!@#";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = "{\"Data\":{\"Balance\":[{\"Amount\":{\"Amount\":\"100.00\",\"Currency\":\"GBP\"}}]}}".getBytes();
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
			BalanceTools balanceTools = new BalanceTools(client, objectMapper);

			OBReadBalance1 result = balanceTools.getBalances(accountIdSpecial);

			assertThat(result).isInstanceOf(OBReadBalance1.class);
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
			BalanceTools balanceTools = new BalanceTools(client, objectMapper);

			assertThatThrownBy(() -> balanceTools.getBalances("valid-account-id"))
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
			throw new RuntimeException("Token service unavailable");
		};

		OpenBankingClient client = new OpenBankingClient(failingTokenService, properties);
		BalanceTools balanceTools = new BalanceTools(client, objectMapper);

		assertThatThrownBy(() -> balanceTools.getBalances("valid-account-id"))
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
				throw new RuntimeException("Something went terribly wrong");
			}
		};

		BalanceTools balanceTools = new BalanceTools(client, objectMapper);

		assertThatThrownBy(() -> balanceTools.getBalances("valid-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("An unexpected error occurred - ")
				.hasMessageContaining("Something went terribly wrong");
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

		BalanceTools balanceTools = new BalanceTools(client, objectMapper);

		assertThatThrownBy(() -> balanceTools.getBalances("valid-account-id"))
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
		BalanceTools balanceTools = new BalanceTools(client, objectMapper);

		assertThatThrownBy(() -> balanceTools.getBalances("a".repeat(41)))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("1 and 40 characters");
	}
}
