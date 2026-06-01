package com.dyl.openbanking_mcp.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.client.OpenBankingClientException;
import com.dyl.openbanking_mcp.config.OpenBankingProperties;
import com.dyl.openbanking_mcp.model.OBReadTransaction6;
import com.dyl.openbanking_mcp.token.TokenService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Feature: openbanking-mcp-tools, Property 7: Date-time parameter validation and inclusion
class TransactionToolsTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	/**
	 * Validates: Requirements 7.3, 7.4, 7.8
	 *
	 * For any string that is not a valid ISO 8601 date-time, when provided as
	 * fromBookingDateTime or toBookingDateTime to the get_transactions tool,
	 * the tool SHALL throw a McpToolError indicating invalid date format.
	 */
	@Property
	void invalidDateTimeThrowsMcpToolError(@ForAll("invalidDateTimes") String invalidDateTime) {
		// Create a client with valid properties that would fail if called
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		// TokenService that throws if called — verifies no HTTP call is made
		TokenService failingTokenService = () -> {
			throw new AssertionError("HTTP call should not be made for invalid date-time");
		};

		OpenBankingClient client = new OpenBankingClient(failingTokenService, properties);
		TransactionTools transactionTools = new TransactionTools(client, objectMapper);

		// Test invalid fromBookingDateTime
		assertThatThrownBy(() -> transactionTools.getTransactions("valid-account", invalidDateTime, null, null))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("ISO 8601");

		// Test invalid toBookingDateTime
		assertThatThrownBy(() -> transactionTools.getTransactions("valid-account", null, invalidDateTime, null))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("ISO 8601");
	}

	@Provide
	Arbitrary<String> invalidDateTimes() {
		return Arbitraries.oneOf(
				// Clearly not dates
				Arbitraries.of(
						"not-a-date",
						"yesterday",
						"2024-13-45",
						"2024-02-30T10:00:00Z",
						"2024/01/15",
						"15-01-2024",
						"20240115",
						"10:30:00",
						"2024-01-15 10:30:00",
						"abc123"
				),
				// Random alphanumeric strings
				Arbitraries.strings()
						.withCharRange('a', 'z')
						.ofMinLength(1)
						.ofMaxLength(30),
				// Random numeric strings that look date-like but aren't valid ISO 8601
				Arbitraries.strings()
						.withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', ':')
						.ofMinLength(5)
						.ofMaxLength(20)
		);
	}

	/**
	 * Validates: Requirements 7.3, 7.4, 7.8
	 *
	 * For any valid ISO 8601 date-time string provided as fromBookingDateTime or
	 * toBookingDateTime, the HTTP request SHALL include them as query parameters
	 * with their original values.
	 */
	@Property(tries = 50)
	void validDateTimeIncludedAsQueryParameter(@ForAll("validDateTimes") String validDateTime) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		String[] capturedRawUri = new String[1];

		server.createContext("/", exchange -> {
			capturedRawUri[0] = exchange.getRequestURI().getRawQuery();
			byte[] response = "{\"Data\":{\"Transaction\":[]}}".getBytes();
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
			TransactionTools transactionTools = new TransactionTools(client, objectMapper);

			// Test fromBookingDateTime inclusion
			OBReadTransaction6 fromResult = transactionTools.getTransactions("valid-account", validDateTime, null, null);
			assertThat(fromResult).isInstanceOf(OBReadTransaction6.class);
			String fromQuery = capturedRawUri[0];
			assertThat(fromQuery).contains("fromBookingDateTime=");
			// Decode the query to verify the original value is preserved
			String decodedFromQuery = java.net.URLDecoder.decode(fromQuery, java.nio.charset.StandardCharsets.UTF_8);
			assertThat(decodedFromQuery).contains("fromBookingDateTime=" + validDateTime);

			// Test toBookingDateTime inclusion
			OBReadTransaction6 toResult = transactionTools.getTransactions("valid-account", null, validDateTime, null);
			assertThat(toResult).isInstanceOf(OBReadTransaction6.class);
			String toQuery = capturedRawUri[0];
			assertThat(toQuery).contains("toBookingDateTime=");
			// Decode the query to verify the original value is preserved
			String decodedToQuery = java.net.URLDecoder.decode(toQuery, java.nio.charset.StandardCharsets.UTF_8);
			assertThat(decodedToQuery).contains("toBookingDateTime=" + validDateTime);
		} finally {
			server.stop(0);
		}
	}

	@Provide
	Arbitrary<String> validDateTimes() {
		// Generate valid ISO 8601 date-time strings
		Arbitrary<Integer> years = Arbitraries.integers().between(2020, 2030);
		Arbitrary<Integer> months = Arbitraries.integers().between(1, 12);
		Arbitrary<Integer> days = Arbitraries.integers().between(1, 28); // safe for all months
		Arbitrary<Integer> hours = Arbitraries.integers().between(0, 23);
		Arbitrary<Integer> minutes = Arbitraries.integers().between(0, 59);
		Arbitrary<Integer> seconds = Arbitraries.integers().between(0, 59);
		Arbitrary<String> offsets = Arbitraries.of("Z", "+00:00", "+01:00", "-05:00", "+05:30", "-08:00");

		return Combinators.combine(years, months, days, hours, minutes, seconds, offsets)
				.as((year, month, day, hour, minute, second, offset) ->
						String.format("%04d-%02d-%02dT%02d:%02d:%02d%s",
								year, month, day, hour, minute, second, offset));
	}

	// --- Example-based unit tests ---

	@Test
	void accountIdExactly40Chars_shouldMakeHttpCall() throws IOException {
		String accountId40 = "t".repeat(40);

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			String path = exchange.getRequestURI().getPath();
			assertThat(path).isEqualTo("/accounts/" + accountId40 + "/transactions");
			byte[] response = "{\"Data\":{\"Transaction\":[]}}".getBytes();
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
			TransactionTools transactionTools = new TransactionTools(client, objectMapper);

			OBReadTransaction6 result = transactionTools.getTransactions(accountId40, null, null, null);

			assertThat(result).isInstanceOf(OBReadTransaction6.class);
		} finally {
			server.stop(0);
		}
	}

	@Test
	void accountIdWithSpecialCharacters_shouldMakeHttpCall() throws IOException {
		String accountIdSpecial = "txn-acc_001.test!@#";

		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = "{\"Data\":{\"Transaction\":[{\"Amount\":{\"Amount\":\"50.00\",\"Currency\":\"GBP\"}}]}}".getBytes();
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
			TransactionTools transactionTools = new TransactionTools(client, objectMapper);

			OBReadTransaction6 result = transactionTools.getTransactions(accountIdSpecial, null, null, null);

			assertThat(result).isInstanceOf(OBReadTransaction6.class);
			assertThat(result.data().transaction()).hasSize(1);
			assertThat(result.data().transaction().get(0).amount().amount()).isEqualTo("50.00");
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
			TransactionTools transactionTools = new TransactionTools(client, objectMapper);

			assertThatThrownBy(() -> transactionTools.getTransactions("test-account", null, null, null))
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
			throw new RuntimeException("Token refresh failed");
		};

		OpenBankingClient client = new OpenBankingClient(failingTokenService, properties);
		TransactionTools transactionTools = new TransactionTools(client, objectMapper);

		assertThatThrownBy(() -> transactionTools.getTransactions("test-account", null, null, null))
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
			public String get(String path, java.util.Map<String, String> queryParams) {
				throw new RuntimeException("Unexpected transaction error");
			}
		};

		TransactionTools transactionTools = new TransactionTools(client, objectMapper);

		assertThatThrownBy(() -> transactionTools.getTransactions("test-account", null, null, null))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("An unexpected error occurred - ")
				.hasMessageContaining("Unexpected transaction error");
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
			public String get(String path, java.util.Map<String, String> queryParams) {
				throw new OpenBankingClientException("Unable to connect to Open Banking API - connection timed out after 30 seconds");
			}
		};

		TransactionTools transactionTools = new TransactionTools(client, objectMapper);

		assertThatThrownBy(() -> transactionTools.getTransactions("test-account", null, null, null))
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
		TransactionTools transactionTools = new TransactionTools(client, objectMapper);

		assertThatThrownBy(() -> transactionTools.getTransactions("a".repeat(41), null, null, null))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("1 and 40 characters");
	}

	@Test
	void xFapiInteractionIdStripped_shouldDeserializeSuccessfully() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		int port = server.getAddress().getPort();

		server.createContext("/", exchange -> {
			byte[] response = "{\"Data\":{\"Transaction\":[]}}".getBytes();
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
			TransactionTools transactionTools = new TransactionTools(client, objectMapper);

			OBReadTransaction6 result = transactionTools.getTransactions("test-account", null, null, null);

			assertThat(result).isInstanceOf(OBReadTransaction6.class);
			assertThat(result.data().transaction()).isEmpty();
		} finally {
			server.stop(0);
		}
	}
}
