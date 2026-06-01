package com.dyl.openbanking_mcp.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "openbanking.api")
@Validated
public class OpenBankingProperties {

	@NotBlank(message = "openbanking.api.base-url is required")
	@Pattern(regexp = "^https://.*", message = "openbanking.api.base-url must be a valid HTTPS URL")
	private String baseUrl;

	private int connectTimeout = 30;

	private int requestTimeout = 30;

	@PostConstruct
	public void normaliseBaseUrl() {
		if (baseUrl != null) {
			while (baseUrl.endsWith("/")) {
				baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
			}
		}
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
}
