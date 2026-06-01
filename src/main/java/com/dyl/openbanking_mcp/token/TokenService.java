package com.dyl.openbanking_mcp.token;

/**
 * Service interface for retrieving OAuth2 Bearer tokens for Open Banking API requests.
 *
 * <p>Implementations are responsible for obtaining a valid access token from the
 * appropriate OAuth2 authorization server. Tokens may be cached, refreshed, or
 * retrieved on-demand depending on the implementation strategy.</p>
 *
 * <p>The returned token is used by the HTTP client layer to set the
 * {@code Authorization: Bearer <token>} header on all outbound requests to the
 * Open Banking API (ASPSP).</p>
 */
public interface TokenService {

	/**
	 * Retrieves a valid Bearer token for Open Banking API requests.
	 *
	 * @return a non-blank Bearer token string
	 * @throws UnsupportedOperationException if the implementation is a placeholder
	 * @throws RuntimeException if the token cannot be obtained for any reason
	 */
	String getToken();
}
