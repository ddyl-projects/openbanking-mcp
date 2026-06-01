package com.dyl.openbanking_mcp.tools;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.client.OpenBankingClientException;
import com.dyl.openbanking_mcp.model.OBReadTransaction6;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * MCP tool methods for Open Banking Transaction operations.
 *
 * <p>Exposes {@code get_transactions} tool that proxies requests to the
 * Open Banking API via {@link OpenBankingClient}.</p>
 */
@Component
public class TransactionTools {

	private final OpenBankingClient openBankingClient;
	private final ObjectMapper objectMapper;

	public TransactionTools(OpenBankingClient openBankingClient, ObjectMapper objectMapper) {
		this.openBankingClient = openBankingClient;
		this.objectMapper = objectMapper;
	}

	/**
	 * Retrieves transactions for a specific account with optional date filtering.
	 *
	 * @param accountId           the account identifier (1-40 characters)
	 * @param fromBookingDateTime optional start date-time in ISO 8601 format
	 * @param toBookingDateTime   optional end date-time in ISO 8601 format
	 * @return typed {@link OBReadTransaction6} on success, or an error message string on failure
	 */
	@McpTool(name = "get_transactions", description = "Retrieve transactions for a specific account with optional date filtering and pagination")
	public OBReadTransaction6 getTransactions(
			@McpToolParam(description = "Account identifier (1-40 characters)", required = true)
			String accountId,
			@McpToolParam(description = "Start date-time filter in ISO 8601 format (optional)", required = false)
			String fromBookingDateTime,
			@McpToolParam(description = "End date-time filter in ISO 8601 format (optional)", required = false)
			String toBookingDateTime,
			@McpToolParam(description = "Page cursor token from the Links.Next field of a previous response (optional)", required = false)
			String page) {
		if (accountId == null || accountId.isBlank() || accountId.length() > 40) {
			throw new McpToolError("accountId is required and must be between 1 and 40 characters");
		}

		if (fromBookingDateTime != null && !fromBookingDateTime.isEmpty()) {
			if (!isValidIso8601DateTime(fromBookingDateTime)) {
				throw new McpToolError("fromBookingDateTime must be a valid ISO 8601 date-time format");
			}
		}

		if (toBookingDateTime != null && !toBookingDateTime.isEmpty()) {
			if (!isValidIso8601DateTime(toBookingDateTime)) {
				throw new McpToolError("toBookingDateTime must be a valid ISO 8601 date-time format");
			}
		}

		Map<String, String> queryParams = new HashMap<>();
		if (fromBookingDateTime != null && !fromBookingDateTime.isEmpty()) {
			queryParams.put("fromBookingDateTime", fromBookingDateTime);
		}
		if (toBookingDateTime != null && !toBookingDateTime.isEmpty()) {
			queryParams.put("toBookingDateTime", toBookingDateTime);
		}
		if (page != null && !page.isEmpty()) {
			queryParams.put("page", page);
		}

		try {
			String response = openBankingClient.get("/accounts/" + accountId + "/transactions", queryParams);
			String json = extractJsonBody(response);
			return objectMapper.readValue(json, OBReadTransaction6.class);
		} catch (OpenBankingClientException e) {
			throw new McpToolError(e.getMessage(), e);
		} catch (JsonProcessingException e) {
			throw new McpToolError("Failed to parse API response - " + e.getMessage(), e);
		} catch (RuntimeException e) {
			throw new McpToolError("An unexpected error occurred - " + e.getMessage(), e);
		}
	}

	private String extractJsonBody(String response) {
		int markerIndex = response.indexOf("\n[x-fapi-interaction-id:");
		if (markerIndex >= 0) {
			return response.substring(0, markerIndex);
		}
		return response;
	}

	private boolean isValidIso8601DateTime(String dateTime) {
		try {
			DateTimeFormatter.ISO_DATE_TIME.parse(dateTime);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}
}
