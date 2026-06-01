package com.dyl.openbanking_mcp.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Unit tests for {@link JacksonConfig}.
 * Validates: Requirements 5.2
 */
class JacksonConfigTests {

	private final JacksonConfig jacksonConfig = new JacksonConfig();

	@Test
	void objectMapperBeanIsCreated() {
		ObjectMapper objectMapper = jacksonConfig.objectMapper();

		assertThat(objectMapper).isNotNull();
	}

	@Test
	void objectMapperHasFailOnUnknownPropertiesDisabled() {
		ObjectMapper objectMapper = jacksonConfig.objectMapper();

		boolean failOnUnknown = objectMapper
				.getDeserializationConfig()
				.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		assertThat(failOnUnknown).isFalse();
	}

	@Test
	void objectMapperIgnoresUnknownFieldsDuringDeserialization() {
		ObjectMapper objectMapper = jacksonConfig.objectMapper();
		String jsonWithUnknownField = """
				{"known": "value", "unknown": "extra"}
				""";

		assertThatNoException().isThrownBy(() ->
				objectMapper.readValue(jsonWithUnknownField, SimpleRecord.class));
	}

	/**
	 * Simple test record to verify deserialization behavior.
	 */
	private record SimpleRecord(String known) {}
}
