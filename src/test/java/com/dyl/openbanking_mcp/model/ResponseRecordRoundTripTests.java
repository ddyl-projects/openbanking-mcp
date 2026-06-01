package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;

import java.util.List;

// Feature: mcp-response-schema, Property 1: JSON deserialization round-trip
class ResponseRecordRoundTripTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	// **Validates: Requirements 5.1, 5.3**
	@Property(tries = 100)
	void obReadAccount6RoundTrip(@ForAll("obReadAccount6Arbitrary") OBReadAccount6 original) throws Exception {
		String json = objectMapper.writeValueAsString(original);
		OBReadAccount6 deserialized = objectMapper.readValue(json, OBReadAccount6.class);
		assert deserialized.equals(original) : "Round-trip failed: original != deserialized";
	}

	@Provide
	Arbitrary<OBReadAccount6> obReadAccount6Arbitrary() {
		return Combinators.combine(
				dataArbitrary(),
				linksArbitrary(),
				metaArbitrary()
		).as(OBReadAccount6::new);
	}

	@Provide
	Arbitrary<OBReadAccount6.Data> dataArbitrary() {
		return obAccount6Arbitrary().list().ofMinSize(0).ofMaxSize(5)
				.map(OBReadAccount6.Data::new);
	}

	@Provide
	Arbitrary<OBAccount6> obAccount6Arbitrary() {
		Arbitrary<String> optionalString = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
				.injectNull(0.3);
		Arbitrary<String> requiredString = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);

		return Combinators.combine(
				requiredString,   // accountId
				optionalString,   // status
				optionalString,   // currency
				optionalString,   // accountCategory
				optionalString,   // accountTypeCode
				optionalString,   // description
				optionalString,   // nickname
				optionalString    // openingDate
		).as((accountId, status, currency, accountCategory, accountTypeCode, description, nickname, openingDate) ->
				new Object[]{accountId, status, currency, accountCategory, accountTypeCode, description, nickname, openingDate}
		).flatMap(first8 -> Combinators.combine(
				optionalString,   // maturityDate
				optionalString,   // switchStatus
				obAccountIdentifierArbitrary().list().ofMinSize(0).ofMaxSize(3).injectNull(0.2),
				obServicerArbitrary().injectNull(0.3)
		).as((maturityDate, switchStatus, account, servicer) ->
				new OBAccount6(
						(String) first8[0],
						(String) first8[1],
						(String) first8[2],
						(String) first8[3],
						(String) first8[4],
						(String) first8[5],
						(String) first8[6],
						(String) first8[7],
						maturityDate,
						switchStatus,
						(List<OBAccountIdentifier>) account,
						servicer
				)
		));
	}

	@Provide
	Arbitrary<OBAccountIdentifier> obAccountIdentifierArbitrary() {
		Arbitrary<String> optionalString = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
				.injectNull(0.3);
		Arbitrary<String> requiredString = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);

		return Combinators.combine(
				requiredString,   // schemeName
				requiredString,   // identification
				optionalString,   // name
				optionalString,   // lei
				optionalString    // secondaryIdentification
		).as(OBAccountIdentifier::new);
	}

	@Provide
	Arbitrary<OBServicer> obServicerArbitrary() {
		Arbitrary<String> optionalString = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
				.injectNull(0.3);

		return Combinators.combine(
				optionalString,   // schemeName
				optionalString    // identification
		).as(OBServicer::new);
	}

	@Provide
	Arbitrary<Links> linksArbitrary() {
		Arbitrary<String> optionalString = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
				.injectNull(0.3);

		return Combinators.combine(
				optionalString,   // self
				optionalString,   // first
				optionalString,   // prev
				optionalString,   // next
				optionalString    // last
		).as(Links::new);
	}

	@Provide
	Arbitrary<Meta> metaArbitrary() {
		Arbitrary<Integer> optionalInt = Arbitraries.integers().between(1, 100).injectNull(0.3);
		Arbitrary<String> optionalString = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
				.injectNull(0.3);

		return Combinators.combine(
				optionalInt,      // totalPages
				optionalString,   // firstAvailableDateTime
				optionalString    // lastAvailableDateTime
		).as(Meta::new);
	}
}
