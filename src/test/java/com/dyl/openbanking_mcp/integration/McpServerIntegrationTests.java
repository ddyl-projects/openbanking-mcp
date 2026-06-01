package com.dyl.openbanking_mcp.integration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.dyl.openbanking_mcp.OpenbankingMcpApplication;
import com.dyl.openbanking_mcp.tools.AccountTools;
import com.dyl.openbanking_mcp.tools.BalanceTools;
import com.dyl.openbanking_mcp.tools.ProductTools;
import com.dyl.openbanking_mcp.tools.TransactionTools;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MCP server tool discovery and startup behaviour.
 *
 * <p>Validates that the Spring context loads correctly with valid configuration,
 * all 5 MCP tools are registered and discoverable, and that invalid configuration
 * prevents the application from starting.</p>
 */
@SpringBootTest(properties = {
	"openbanking.api.base-url=https://api.example.com",
	"openbanking.api.token=test-token-for-integration"
})
class McpServerIntegrationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private McpSyncServer mcpSyncServer;

	@Test
	void contextLoadsWithValidConfiguration() {
		assertNotNull(applicationContext);
		assertNotNull(mcpSyncServer);
	}

	@Test
	void allFiveToolsAreRegisteredAndDiscoverable() {
		// Verify all tool component beans are present in the context
		assertNotNull(applicationContext.getBean(AccountTools.class));
		assertNotNull(applicationContext.getBean(BalanceTools.class));
		assertNotNull(applicationContext.getBean(ProductTools.class));
		assertNotNull(applicationContext.getBean(TransactionTools.class));

		// Verify all 5 tools are registered with the MCP server
		List<McpSchema.Tool> tools = mcpSyncServer.listTools();
		Set<String> toolNames = tools.stream()
				.map(McpSchema.Tool::name)
				.collect(Collectors.toSet());

		assertEquals(5, toolNames.size(), "Expected exactly 5 MCP tools to be registered");
		assertTrue(toolNames.contains("list_accounts"), "list_accounts tool should be registered");
		assertTrue(toolNames.contains("get_account"), "get_account tool should be registered");
		assertTrue(toolNames.contains("get_balances"), "get_balances tool should be registered");
		assertTrue(toolNames.contains("get_product"), "get_product tool should be registered");
		assertTrue(toolNames.contains("get_transactions"), "get_transactions tool should be registered");
	}

	@Test
	void invalidConfigurationMissingBaseUrlPreventsContextFromLoading() {
		assertThrows(Exception.class, () ->
				new SpringApplicationBuilder(OpenbankingMcpApplication.class)
						.properties("openbanking.api.base-url=")
						.run()
		);
	}
}
