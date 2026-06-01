package com.dyl.openbanking_mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.dyl.openbanking_mcp.config.OpenBankingProperties;

@SpringBootApplication
@EnableConfigurationProperties(OpenBankingProperties.class)
public class OpenbankingMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenbankingMcpApplication.class, args);
	}

}
