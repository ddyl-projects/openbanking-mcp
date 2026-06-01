package com.dyl.openbanking_mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.dyl.openbanking_mcp.client.OpenBankingClient;
import com.dyl.openbanking_mcp.client.OpenBankingClientException;
import com.dyl.openbanking_mcp.model.OBReadAccount6;

/**
 * MCP tool methods for Open Banking Account operations.
 *
 * <p>Exposes {@code list_accounts} and {@code get_account} tools that proxy
 * requests to the Open Banking API via {@link OpenBankingClient}.</p>
 *
 * <p>Returns typed {@link OBReadAccount6} records on success. Spring AI generates
 * an {@code outputSchema} in the MCP tool definition from the return type.
 * Errors are thrown as exceptions, which Spring AI converts to MCP error responses
 * with {@code isError: true}.</p>
 */
@Component
public class AccountTools {

	private final OpenBankingClient openBankingClient;
	private final ObjectMapper objectMapper;

	public AccountTools(OpenBankingClient openBankingClient, ObjectMapper objectMapper) {
		this.openBankingClient = openBankingClient;
		this.objectMapper = objectMapper;
	}

	/**
	 * Retrieves all PSU accounts from the Open Banking API.
	 *
	 * @return {@link OBReadAccount6} containing account data, links, and metadata
	 */
	@McpTool(name = "list_accounts", description = "Retrieve all PSU accounts from the Open Banking API")
	public OBReadAccount6 listAccounts() {
		try {
			String response = openBankingClient.get("/accounts");
			String json = extractJsonBody(response);
			return objectMapper.readValue(json, OBReadAccount6.class);
		} catch (OpenBankingClientException e) {
			throw new McpToolError(e.getMessage(), e);
		} catch (JsonProcessingException e) {
			throw new McpToolError("Failed to parse API response - " + e.getMessage(), e);
		} catch (RuntimeException e) {
			throw new McpToolError("An unexpected error occurred - " + e.getMessage(), e);
		}
	}

	/**
	 * Retrieves details for a specific account by AccountId.
	 *
	 * @param accountId the account identifier (1-40 characters)
	 * @return {@link OBReadAccount6} containing account data, links, and metadata
	 */
	@McpTool(name = "get_account", description = "Retrieve details for a specific account by AccountId")
	public OBReadAccount6 getAccount(
			@McpToolParam(description = "Account identifier (1-40 characters)", required = true)
			String accountId) {
		if (accountId == null || accountId.isBlank() || accountId.length() > 40) {
			throw new McpToolError("accountId is required and must be between 1 and 40 characters");
		}

		try {
			String response = openBankingClient.get("/accounts/" + accountId);
			String json = extractJsonBody(response);
			return objectMapper.readValue(json, OBReadAccount6.class);
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
