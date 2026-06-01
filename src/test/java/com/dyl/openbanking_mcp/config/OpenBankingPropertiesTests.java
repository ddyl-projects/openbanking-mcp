package com.dyl.openbanking_mcp.config;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: openbanking-mcp-tools, Property 1: Base URL trailing slash normalisation
class OpenBankingPropertiesTests {

	/**
	 * **Validates: Requirements 1.3**
	 *
	 * For any valid HTTPS URL with arbitrary trailing slashes,
	 * verify normalised URL does not end with '/'.
	 */
	@Property
	void normalisedBaseUrlNeverEndsWithTrailingSlash(
			@ForAll("httpsUrlsWithTrailingSlashes") String urlWithSlashes) {
		OpenBankingProperties properties = new OpenBankingProperties();
		properties.setBaseUrl(urlWithSlashes);

		properties.normaliseBaseUrl();

		assertThat(properties.getBaseUrl()).doesNotEndWith("/");
	}

	@Provide
	Arbitrary<String> httpsUrlsWithTrailingSlashes() {
		Arbitrary<String> domains = Arbitraries.strings()
				.alpha()
				.ofMinLength(1)
				.ofMaxLength(20)
				.map(String::toLowerCase);

		Arbitrary<String> paths = Arbitraries.strings()
				.withCharRange('a', 'z')
				.withChars('-', '_')
				.ofMinLength(0)
				.ofMaxLength(30)
				.map(p -> p.isEmpty() ? "" : "/" + p);

		Arbitrary<Integer> slashCount = Arbitraries.integers().between(1, 10);

		return Combinators.combine(domains, paths, slashCount)
				.as((domain, path, numSlashes) ->
						"https://" + domain + ".example.com" + path + "/".repeat(numSlashes));
	}
}
