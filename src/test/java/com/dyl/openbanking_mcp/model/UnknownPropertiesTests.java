package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;

import java.util.*;

// Feature: mcp-response-schema, Property 2: Unknown JSON properties tolerance
class UnknownPropertiesTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	// **Validates: Requirements 5.2**
	@Property(tries = 100)
	void obReadAccount6ToleratesUnknownProperties(
			@ForAll("randomExtraKeys") List<String> extraKeys,
			@ForAll("randomExtraValues") List<String> extraValues
	) throws Exception {
		String validJson = """
				{
					"Data": {
						"Account": [
							{
								"AccountId": "acc-001",
								"Status": "Enabled",
								"Currency": "GBP",
								"Nickname": "Main Account"
							}
						]
					},
					"Links": { "Self": "https://api.bank.com/accounts" },
					"Meta": { "TotalPages": 1 }
				}
				""";

		String injectedJson = injectUnknownProperties(validJson, extraKeys, extraValues);
		OBReadAccount6 result = objectMapper.readValue(injectedJson, OBReadAccount6.class);

		assert result.data() != null : "Data should not be null";
		assert result.data().account() != null : "Account list should not be null";
		assert result.data().account().size() == 1 : "Account list size should be 1";
		assert "acc-001".equals(result.data().account().get(0).accountId()) : "AccountId should be preserved";
		assert "Enabled".equals(result.data().account().get(0).status()) : "Status should be preserved";
		assert "GBP".equals(result.data().account().get(0).currency()) : "Currency should be preserved";
		assert "Main Account".equals(result.data().account().get(0).nickname()) : "Nickname should be preserved";
		assert "https://api.bank.com/accounts".equals(result.links().self()) : "Links.Self should be preserved";
		assert Integer.valueOf(1).equals(result.meta().totalPages()) : "Meta.TotalPages should be preserved";
	}

	// **Validates: Requirements 5.2**
	@Property(tries = 100)
	void obReadBalance1ToleratesUnknownProperties(
			@ForAll("randomExtraKeys") List<String> extraKeys,
			@ForAll("randomExtraValues") List<String> extraValues
	) throws Exception {
		String validJson = """
				{
					"Data": {
						"Balance": [
							{
								"AccountId": "acc-002",
								"CreditDebitIndicator": "Credit",
								"Type": "InterimAvailable",
								"DateTime": "2024-01-15T10:00:00Z",
								"Amount": {
									"Amount": "1500.00",
									"Currency": "GBP"
								}
							}
						]
					},
					"Links": { "Self": "https://api.bank.com/balances" },
					"Meta": { "TotalPages": 1 }
				}
				""";

		String injectedJson = injectUnknownProperties(validJson, extraKeys, extraValues);
		OBReadBalance1 result = objectMapper.readValue(injectedJson, OBReadBalance1.class);

		assert result.data() != null : "Data should not be null";
		assert result.data().balance() != null : "Balance list should not be null";
		assert result.data().balance().size() == 1 : "Balance list size should be 1";
		OBBalance1 balance = result.data().balance().get(0);
		assert "acc-002".equals(balance.accountId()) : "AccountId should be preserved";
		assert "Credit".equals(balance.creditDebitIndicator()) : "CreditDebitIndicator should be preserved";
		assert "InterimAvailable".equals(balance.type()) : "Type should be preserved";
		assert "2024-01-15T10:00:00Z".equals(balance.dateTime()) : "DateTime should be preserved";
		assert "1500.00".equals(balance.amount().amount()) : "Amount should be preserved";
		assert "GBP".equals(balance.amount().currency()) : "Currency should be preserved";
	}

	// **Validates: Requirements 5.2**
	@Property(tries = 100)
	void obReadProduct2ToleratesUnknownProperties(
			@ForAll("randomExtraKeys") List<String> extraKeys,
			@ForAll("randomExtraValues") List<String> extraValues
	) throws Exception {
		String validJson = """
				{
					"Data": {
						"Product": [
							{
								"AccountId": "acc-003",
								"ProductId": "prod-001",
								"ProductName": "Current Account",
								"ProductType": "PersonalCurrentAccount"
							}
						]
					},
					"Links": { "Self": "https://api.bank.com/products" },
					"Meta": { "TotalPages": 1 }
				}
				""";

		String injectedJson = injectUnknownProperties(validJson, extraKeys, extraValues);
		OBReadProduct2 result = objectMapper.readValue(injectedJson, OBReadProduct2.class);

		assert result.data() != null : "Data should not be null";
		assert result.data().product() != null : "Product list should not be null";
		assert result.data().product().size() == 1 : "Product list size should be 1";
		OBProduct2 product = result.data().product().get(0);
		assert "acc-003".equals(product.accountId()) : "AccountId should be preserved";
		assert "prod-001".equals(product.productId()) : "ProductId should be preserved";
		assert "Current Account".equals(product.productName()) : "ProductName should be preserved";
		assert "PersonalCurrentAccount".equals(product.productType()) : "ProductType should be preserved";
	}

	// **Validates: Requirements 5.2**
	@Property(tries = 100)
	void obReadTransaction6ToleratesUnknownProperties(
			@ForAll("randomExtraKeys") List<String> extraKeys,
			@ForAll("randomExtraValues") List<String> extraValues
	) throws Exception {
		String validJson = """
				{
					"Data": {
						"Transaction": [
							{
								"AccountId": "acc-004",
								"TransactionId": "txn-001",
								"CreditDebitIndicator": "Debit",
								"Status": "Booked",
								"BookingDateTime": "2024-01-15T14:30:00Z",
								"Amount": {
									"Amount": "25.99",
									"Currency": "GBP"
								}
							}
						]
					},
					"Links": { "Self": "https://api.bank.com/transactions" },
					"Meta": { "TotalPages": 2 }
				}
				""";

		String injectedJson = injectUnknownProperties(validJson, extraKeys, extraValues);
		OBReadTransaction6 result = objectMapper.readValue(injectedJson, OBReadTransaction6.class);

		assert result.data() != null : "Data should not be null";
		assert result.data().transaction() != null : "Transaction list should not be null";
		assert result.data().transaction().size() == 1 : "Transaction list size should be 1";
		OBTransaction6 txn = result.data().transaction().get(0);
		assert "acc-004".equals(txn.accountId()) : "AccountId should be preserved";
		assert "txn-001".equals(txn.transactionId()) : "TransactionId should be preserved";
		assert "Debit".equals(txn.creditDebitIndicator()) : "CreditDebitIndicator should be preserved";
		assert "Booked".equals(txn.status()) : "Status should be preserved";
		assert "2024-01-15T14:30:00Z".equals(txn.bookingDateTime()) : "BookingDateTime should be preserved";
		assert "25.99".equals(txn.amount().amount()) : "Amount should be preserved";
		assert "GBP".equals(txn.amount().currency()) : "Currency should be preserved";
		assert Integer.valueOf(2).equals(result.meta().totalPages()) : "Meta.TotalPages should be preserved";
	}

	@Provide
	Arbitrary<List<String>> randomExtraKeys() {
		return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15)
				.list().ofMinSize(1).ofMaxSize(5);
	}

	@Provide
	Arbitrary<List<String>> randomExtraValues() {
		return Arbitraries.strings().ascii().ofMinLength(0).ofMaxLength(30)
				.list().ofMinSize(1).ofMaxSize(5);
	}

	/**
	 * Injects random extra key-value pairs at various nesting levels in the JSON.
	 * Parses JSON into a Map, adds extra properties at top level, inside Data, and inside nested objects.
	 */
	@SuppressWarnings("unchecked")
	private String injectUnknownProperties(String json, List<String> extraKeys, List<String> extraValues) throws Exception {
		Map<String, Object> root = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

		// Inject at top level
		injectExtras(root, extraKeys, extraValues, "top_");

		// Inject inside Data object
		Object dataObj = root.get("Data");
		if (dataObj instanceof Map) {
			Map<String, Object> data = (Map<String, Object>) dataObj;
			injectExtras(data, extraKeys, extraValues, "data_");

			// Inject inside nested list items (Account, Balance, Product, Transaction)
			for (Map.Entry<String, Object> entry : new ArrayList<>(data.entrySet())) {
				if (entry.getValue() instanceof List) {
					List<Object> items = (List<Object>) entry.getValue();
					for (Object item : items) {
						if (item instanceof Map) {
							Map<String, Object> itemMap = (Map<String, Object>) item;
							injectExtras(itemMap, extraKeys, extraValues, "nested_");

							// Inject inside deeply nested objects (e.g., Amount)
							for (Map.Entry<String, Object> nestedEntry : new ArrayList<>(itemMap.entrySet())) {
								if (nestedEntry.getValue() instanceof Map) {
									Map<String, Object> nestedMap = (Map<String, Object>) nestedEntry.getValue();
									injectExtras(nestedMap, extraKeys, extraValues, "deep_");
								}
							}
						}
					}
				}
			}
		}

		return objectMapper.writeValueAsString(root);
	}

	private void injectExtras(Map<String, Object> target, List<String> keys, List<String> values, String prefix) {
		int count = Math.min(keys.size(), values.size());
		for (int i = 0; i < count; i++) {
			target.put(prefix + keys.get(i), values.get(i));
		}
	}
}
