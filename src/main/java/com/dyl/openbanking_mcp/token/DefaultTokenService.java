package com.dyl.openbanking_mcp.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Token service that reads the Bearer token from application properties.
 *
 * <p>Retrieves the token from the {@code openbanking.api.token} property,
 * which can be set via environment variable {@code OPENBANKING_API_TOKEN}
 * or directly in application.yaml.</p>
 */
@Component
public class DefaultTokenService implements TokenService {

	private final String token;

	public DefaultTokenService(@Value("${openbanking.api.token}") String token) {
		this.token = token;
	}

	@Override
	public String getToken() {
		if (token == null || token.isBlank()) {
			throw new IllegalStateException("Authentication unavailable - openbanking.api.token is not configured");
		}
		return token;
	}
}
