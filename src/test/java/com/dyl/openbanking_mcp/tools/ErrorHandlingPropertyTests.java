package com.dyl.openbanking_mcp.tools;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.client.OpenBankingClientException;
import com.dyl.openbanking_mcp.config.OpenBankingProperties;
import com.dyl.openbanking_mcp.token.TokenService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Feature: mcp-response-schema, Property 4: Error handling preservation
class ErrorHandlingPropertyTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private OpenBankingProperties createProperties() {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl("https://api.example.com");
		properties.setConnectTimeout(5);
		properties.setRequestTimeout(5);
		return properties;
	}

	private TokenService createTokenService() {
		return () -> "test-token";
	}

	private OpenBankingClient createClientThrowingOpenBankingClientException(String message) {
		return new OpenBankingClient(createTokenService(), createProperties()) {
			@Override
			public String get(String path) {
				throw new OpenBankingClientException(message);
			}

			@Override
			public String get(String path, Map<String, String> queryParams) {
				throw new OpenBankingClientException(message);
			}
		};
	}

	private OpenBankingClient createClientThrowingRuntimeException(String message) {
		return new OpenBankingClient(createTokenService(), createProperties()) {
			@Override
			public String get(String path) {
				throw new RuntimeException(message);
			}

			@Override
			public String get(String path, Map<String, String> queryParams) {
				throw new RuntimeException(message);
			}
		};
	}

	// **Validates: Requirements 8.1, 8.2, 8.3**

	@Property(tries = 100)
	void accountTools_openBankingClientException_throwsMcpToolErrorWithMessage(
			@ForAll("exceptionMessages") String message) {
		OpenBankingClient client = createClientThrowingOpenBankingClientException(message);
		AccountTools tools = new AccountTools(client, objectMapper);

		assertThatThrownBy(() -> tools.listAccounts())
				.isInstanceOf(McpToolError.class)
				.hasMessage(message);
	}

	@Property(tries = 100)
	void accountTools_runtimeException_throwsMcpToolErrorWithPrefixedMessage(
			@ForAll("exceptionMessages") String message) {
		OpenBankingClient client = createClientThrowingRuntimeException(message);
		AccountTools tools = new AccountTools(client, objectMapper);

		assertThatThrownBy(() -> tools.listAccounts())
				.isInstanceOf(McpToolError.class)
				.hasMessage("An unexpected error occurred - " + message);
	}

	@Property(tries = 100)
	void balanceTools_openBankingClientException_throwsMcpToolErrorWithMessage(
			@ForAll("exceptionMessages") String message) {
		OpenBankingClient client = createClientThrowingOpenBankingClientException(message);
		BalanceTools tools = new BalanceTools(client, objectMapper);

		assertThatThrownBy(() -> tools.getBalances("valid-account"))
				.isInstanceOf(McpToolError.class)
				.hasMessage(message);
	}

	@Property(tries = 100)
	void balanceTools_runtimeException_throwsMcpToolErrorWithPrefixedMessage(
			@ForAll("exceptionMessages") String message) {
		OpenBankingClient client = createClientThrowingRuntimeException(message);
		BalanceTools tools = new BalanceTools(client, objectMapper);

		assertThatThrownBy(() -> tools.getBalances("valid-account"))
				.isInstanceOf(McpToolError.class)
				.hasMessage("An unexpected error occurred - " + message);
	}

	@Property(tries = 100)
	void productTools_openBankingClientException_throwsMcpToolErrorWithMessage(
			@ForAll("exceptionMessages") String message) {
		OpenBankingClient client = createClientThrowingOpenBankingClientException(message);
		ProductTools tools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> tools.getProduct("valid-account"))
				.isInstanceOf(McpToolError.class)
				.hasMessage(message);
	}

	@Property(tries = 100)
	void productTools_runtimeException_throwsMcpToolErrorWithPrefixedMessage(
			@ForAll("exceptionMessages") String message) {
		OpenBankingClient client = createClientThrowingRuntimeException(message);
		ProductTools tools = new ProductTools(client, objectMapper);

		assertThatThrownBy(() -> tools.getProduct("valid-account"))
				.isInstanceOf(McpToolError.class)
				.hasMessage("An unexpected error occurred - " + message);
	}

	@Property(tries = 100)
	void transactionTools_openBankingClientException_throwsMcpToolErrorWithMessage(
			@ForAll("exceptionMessages") String message) {
		OpenBankingClient client = createClientThrowingOpenBankingClientException(message);
		TransactionTools tools = new TransactionTools(client, objectMapper);

		assertThatThrownBy(() -> tools.getTransactions("valid-account", null, null, null))
				.isInstanceOf(McpToolError.class)
				.hasMessage(message);
	}

	@Property(tries = 100)
	void transactionTools_runtimeException_throwsMcpToolErrorWithPrefixedMessage(
			@ForAll("exceptionMessages") String message) {
		OpenBankingClient client = createClientThrowingRuntimeException(message);
		TransactionTools tools = new TransactionTools(client, objectMapper);

		assertThatThrownBy(() -> tools.getTransactions("valid-account", null, null, null))
				.isInstanceOf(McpToolError.class)
				.hasMessage("An unexpected error occurred - " + message);
	}

	@Provide
	Arbitrary<String> exceptionMessages() {
		Arbitrary<String> alphanumeric = Arbitraries.strings()
				.withCharRange('a', 'z')
				.withCharRange('A', 'Z')
				.withCharRange('0', '9')
				.ofMinLength(1)
				.ofMaxLength(100);

		Arbitrary<String> withSpaces = Arbitraries.strings()
				.withCharRange('a', 'z')
				.withCharRange('A', 'Z')
				.withCharRange('0', '9')
				.withChars(' ')
				.ofMinLength(1)
				.ofMaxLength(100);

		Arbitrary<String> withSpecialChars = Arbitraries.strings()
				.withCharRange('a', 'z')
				.withCharRange('A', 'Z')
				.withCharRange('0', '9')
				.withChars(' ', '-', '_', '.', ':', '/', '!', '@', '#', '$', '%')
				.ofMinLength(1)
				.ofMaxLength(100);

		return Arbitraries.oneOf(alphanumeric, withSpaces, withSpecialChars);
	}
}
