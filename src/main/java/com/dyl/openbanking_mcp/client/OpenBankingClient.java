package com.dyl.openbanking_mcp.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dyl.openbanking_mcp.config.OpenBankingProperties;
import com.dyl.openbanking_mcp.token.TokenService;

/**
 * HTTP client service that sends requests to the Open Banking API (ASPSP).
 *
 * <p>Centralises HTTP concerns including authentication, FAPI headers,
 * timeouts, and error handling for all Open Banking API interactions.</p>
 */
@Service
public class OpenBankingClient {

	private static final Logger logger = LoggerFactory.getLogger(OpenBankingClient.class);

	private final HttpClient httpClient;
	private final TokenService tokenService;
	private final OpenBankingProperties properties;

	public OpenBankingClient(TokenService tokenService, OpenBankingProperties properties) {
		this.tokenService = tokenService;
		this.properties = properties;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(properties.getConnectTimeout()))
				.build();
	}

	/**
	 * Sends a GET request to the specified path on the Open Banking API.
	 *
	 * @param path the API path (e.g., "/accounts")
	 * @return the response body on success, or a structured error string on non-2xx responses
	 * @throws OpenBankingClientException if the token is unavailable or a connectivity failure occurs
	 */
	public String get(String path) {
		return get(path, null);
	}

	/**
	 * Sends a GET request to the specified path with optional query parameters.
	 *
	 * @param path        the API path (e.g., "/accounts/{id}/transactions")
	 * @param queryParams optional query parameters to append to the URL
	 * @return the response body on success, or a structured error string on non-2xx responses
	 * @throws OpenBankingClientException if the token is unavailable or a connectivity failure occurs
	 */
	public String get(String path, Map<String, String> queryParams) {
		logger.info("Calling path: {} with query params: {}", path, queryParams);
		String token = retrieveToken();

		if (token == null || token.isBlank()) {
			throw new OpenBankingClientException(
					"Authentication unavailable - unable to obtain access token");
		}

		String url = buildUrl(path, queryParams);
		String interactionId = UUID.randomUUID().toString();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(properties.getRequestTimeout()))
				.header("Authorization", "Bearer " + token)
				.header("Accept", "application/json")
				.header("x-fapi-interaction-id", interactionId)
				.GET()
				.build();

		logger.debug("Sending GET request to {} with x-fapi-interaction-id: {}", url, interactionId);

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			return handleResponse(response);
		}
		catch (java.net.http.HttpTimeoutException e) {
			throw new OpenBankingClientException(
					"Unable to connect to Open Banking API - connection timed out after "
							+ properties.getRequestTimeout() + " seconds", e);
		}
		catch (IOException e) {
			throw new OpenBankingClientException(
					"Unable to connect to Open Banking API - " + e.getMessage(), e);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new OpenBankingClientException(
					"Request to Open Banking API was interrupted", e);
		}
	}

	private String retrieveToken() {
		try {
			return CompletableFuture.supplyAsync(() -> tokenService.getToken())
					.get(5, TimeUnit.SECONDS);
		}
		catch (TimeoutException e) {
			throw new OpenBankingClientException(
					"Authentication unavailable - token retrieval timed out after 5 seconds", e);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new OpenBankingClientException(
					"Authentication unavailable - token retrieval was interrupted", e);
		}
		catch (ExecutionException e) {
			throw new OpenBankingClientException(
					"Authentication unavailable - unable to obtain access token", e.getCause());
		}
	}

	private String buildUrl(String path, Map<String, String> queryParams) {
		StringBuilder url = new StringBuilder(properties.getBaseUrl());
		url.append(path);

		if (queryParams != null && !queryParams.isEmpty()) {
			String query = queryParams.entrySet().stream()
					.map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
							+ "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
					.collect(Collectors.joining("&"));
			url.append("?").append(query);
		}

		return url.toString();
	}

	private String handleResponse(HttpResponse<String> response) {
		int statusCode = response.statusCode();
		String body = response.body();

		if (statusCode >= 200 && statusCode < 300) {
			String responseInteractionId = response.headers()
					.firstValue("x-fapi-interaction-id")
					.orElse(null);

			if (responseInteractionId != null) {
				return body + "\n[x-fapi-interaction-id: " + responseInteractionId + "]";
			}
			return body;
		}

		logger.warn("Open Banking API returned HTTP {} for request", statusCode);
		throw new OpenBankingClientException("HTTP " + statusCode + " - " + body);
	}
}
