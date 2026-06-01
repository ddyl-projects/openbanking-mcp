package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBAgent(
	@JsonProperty("SchemeName") @JsonPropertyDescription("Name of the identification scheme, in a coded form as published in an external list") @Nullable String schemeName,
	@JsonProperty("Identification") @JsonPropertyDescription("Unique and unambiguous identification of a financial institution or a branch of a financial institution") @Nullable String identification,
	@JsonProperty("Name") @JsonPropertyDescription("Name by which an agent is known and which is usually used to identify that agent") @Nullable String name,
	@JsonProperty("LEI") @JsonPropertyDescription("Legal entity identification as an alternate identification for a party") @Nullable String lei
) {}
