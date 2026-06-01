package com.dyl.openbanking_mcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"openbanking.api.base-url=https://api.example.com",
	"openbanking.api.token=test-token"
})
class OpenbankingMcpApplicationTests {

	@Test
	void contextLoads() {
	}

}
