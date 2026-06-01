package com.dyl.openbanking_mcp.tools;

import java.util.Map;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.config.OpenBankingProperties;
import com.dyl.openbanking_mcp.token.TokenService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Feature: mcp-response-schema, Property 3: Deserialization failure throws McpToolError
class DeserializationFailurePropertyTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private OpenBankingClient createMockClient(String responseToReturn) {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);

		TokenService tokenService = () -> "test-token";

		return new OpenBankingClient(tokenService, properties) {
			@Override
			public String get(String path) {
				return responseToReturn;
			}

			@Override
			public String get(String path, Map<String, String> queryParams) {
				return responseToReturn;
			}
		};
	}

	/**
	 * Validates: Requirements 5.4
	 *
	 * For any string that is NOT valid JSON, when the tool method attempts to deserialize it,
	 * the tool SHALL throw a McpToolError containing "Failed to parse API response".
	 */
	@Property(tries = 100)
	void accountTools_invalidJson_throwsMcpToolError(@ForAll("invalidJsonStrings") String invalidJson) {
		OpenBankingClient client = createMockClient(invalidJson);
		AccountTools accountTools = new AccountTools(client, objectMapper);

		assertThatThrownBy(() -> accountTools.listAccounts())
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("Failed to parse API response");
	}

	/**
	 * Validates: Requirements 5.4
	 *
	 * For any string that is NOT valid JSON, when the tool method attempts to deserialize it,
	 * the tool SHALL throw a McpToolError containing "Failed to parse API response".
	 */
	@Property(tries = 100)
	void balanceTools_invalidJson_throwsMcpToolError(@ForAll("invalidJsonStrings") String invalidJson) {
		OpenBankingClient client = createMockClient(invalidJson);
		BalanceTools balanceTools = new BalanceTools(client, objectMapper);

		assertThatThrownBy(() -> balanceTools.getBalances("test-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("Failed to parse API response");
	}

	/**
	 * Validates: Requirements 5.4
	 *
	 * For any string that is NOT valid JSON, when the tool method attempts to deserialize it,
	 * the tool SHALL throw a McpToolError containing "Failed to parse API response".
	 */
	@Property(tries = 100)
	void productTools_invalidJson_throwsMcpToolError(@ForAll("invalidJsonStrings") String invalidJson) {
		OpenBankingClient client = createMockClient(invalidJson);
		ProductTools productTools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> productTools.getProduct("test-account-id"))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("Failed to parse API response");
	}

	/**
	 * Validates: Requirements 5.4
	 *
	 * For any string that is NOT valid JSON, when the tool method attempts to deserialize it,
	 * the tool SHALL throw a McpToolError containing "Failed to parse API response".
	 */
	@Property(tries = 100)
	void transactionTools_invalidJson_throwsMcpToolError(@ForAll("invalidJsonStrings") String invalidJson) {
		OpenBankingClient client = createMockClient(invalidJson);
		TransactionTools transactionTools = new TransactionTools(client, objectMapper);

		assertThatThrownBy(() -> transactionTools.getTransactions("test-account-id", null, null, null))
				.isInstanceOf(McpToolError.class)
				.hasMessageContaining("Failed to parse API response");
	}

	@Provide
	Arbitrary<String> invalidJsonStrings() {
		Arbitrary<String> randomBytes = Arbitraries.strings()
				.withCharRange((char) 0, (char) 255)
				.ofMinLength(1)
				.ofMaxLength(100)
				.filter(s -> !isValidJson(s));

		Arbitrary<String> truncatedJson = Arbitraries.of(
				"{\"Data\":{\"Acc",
				"{\"Data\":{\"Account\":[{\"AccountId\":\"abc",
				"{\"Data\":",
				"[{\"key\":\"val",
				"{\"incomplete",
				"{\"Data\":{\"Balance\":[{\"Amount\":",
				"{\"Data\":{\"Product\":[{",
				"{\"Data\":{\"Transaction\":[{\"TransactionId\":"
		);

		Arbitrary<String> xmlFragments = Arbitraries.of(
				"<response><data>test</data></response>",
				"<OBReadAccount6><Data><Account></Account></Data></OBReadAccount6>",
				"<?xml version=\"1.0\"?><root><element>value</element></root>",
				"<accounts><account id=\"123\"><balance>100.00</balance></account></accounts>",
				"<error><code>500</code><message>Internal Error</message></error>"
		);

		Arbitrary<String> plainText = Arbitraries.of(
				"This is not JSON at all",
				"Hello, World!",
				"account balance: 1000.00 GBP",
				"ERROR: something went wrong",
				"true false null undefined NaN",
				"GET /accounts HTTP/1.1",
				"Status: 200 OK",
				"Bearer eyJhbGciOiJSUzI1NiJ9"
		);

		return Arbitraries.oneOf(randomBytes, truncatedJson, xmlFragments, plainText);
	}

	private boolean isValidJson(String s) {
		try {
			objectMapper.readTree(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
