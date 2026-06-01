package com.dyl.openbanking_mcp.client;

/**
 * Custom exception thrown by {@link OpenBankingClient} on timeout or connectivity failures
 * when communicating with the Open Banking API.
 */
public class OpenBankingClientException extends RuntimeException {

	public OpenBankingClientException(String message) {
		super(message);
	}

	public OpenBankingClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
