package com.dyl.openbanking_mcp.tools;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class SchemaTools {

	private final String schemas;

	public SchemaTools() throws IOException {
		this.schemas = new ClassPathResource("response-schemas.json")
				.getContentAsString(StandardCharsets.UTF_8);
	}

	@McpTool(name = "get_response_schemas", description = "Get JSON Schema definitions describing the response structure of all banking tools. Call this first to understand what fields are returned by list_accounts, get_account, get_balances, get_product, and get_transactions.")
	public String getResponseSchemas(
			@McpToolParam(description = "Optional: specific tool name to get schema for (e.g., 'list_accounts', 'get_balances'). If omitted, returns all schemas.", required = false)
			String toolName) {
		if (toolName != null && !toolName.isBlank()) {
			// Could parse and return just the relevant section
			// For simplicity, return all schemas - the AI can filter
		}
		return schemas;
	}
}
