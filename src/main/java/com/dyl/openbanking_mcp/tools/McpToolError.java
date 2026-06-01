package com.dyl.openbanking_mcp.tools;

/**
 * Exception thrown by MCP tool methods to signal an error to the MCP client.
 *
 * <p>Spring AI converts exceptions thrown from {@code @McpTool} methods into
 * MCP error responses with {@code isError: true}, allowing the LLM to see
 * the error and self-correct.</p>
 */
public class McpToolError extends RuntimeException {

	public McpToolError(String message) {
		super(message);
	}

	public McpToolError(String message, Throwable cause) {
		super(message, cause);
	}
}
