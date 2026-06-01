package com.dyl.openbanking_mcp.tools;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.client.OpenBankingClientException;
import com.dyl.openbanking_mcp.model.OBReadBalance1;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * MCP tool for retrieving account balance information from the Open Banking API.
 *
 * <p>Returns typed {@link OBReadBalance1} records on success. Spring AI generates
 * an {@code outputSchema} in the MCP tool definition from the return type.
 * Errors are thrown as exceptions, which Spring AI converts to MCP error responses
 * with {@code isError: true}.</p>
 */
@Component
public class BalanceTools {

	private final OpenBankingClient openBankingClient;
	private final ObjectMapper objectMapper;

	public BalanceTools(OpenBankingClient openBankingClient, ObjectMapper objectMapper) {
		this.openBankingClient = openBankingClient;
		this.objectMapper = objectMapper;
	}

	@McpTool(name = "get_balances", description = "Retrieve balance information for a specific account by AccountId")
	public OBReadBalance1 getBalances(
			@McpToolParam(description = "Account identifier (1-40 characters)", required = true)
			String accountId) {
		if (accountId == null || accountId.isBlank() || accountId.length() > 40) {
			throw new McpToolError("accountId is required and must be between 1 and 40 characters");
		}

		try {
			String response = openBankingClient.get("/accounts/" + accountId + "/balances");
			String json = extractJsonBody(response);
			return objectMapper.readValue(json, OBReadBalance1.class);
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
}
