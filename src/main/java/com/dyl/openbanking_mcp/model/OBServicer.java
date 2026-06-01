package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBServicer(
	@JsonProperty("SchemeName") @JsonPropertyDescription("Name of the identification scheme, in a coded form as published in an external list") @Nullable String schemeName,
	@JsonProperty("Identification") @JsonPropertyDescription("Unique and unambiguous identification of the servicing institution") @Nullable String identification
) {}
