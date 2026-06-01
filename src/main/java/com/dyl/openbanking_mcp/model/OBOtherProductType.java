package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBOtherProductType(
	@JsonProperty("Name") @JsonPropertyDescription("Name of \"Other\" product type") @Nullable String name,
	@JsonProperty("Description") @JsonPropertyDescription("Description of \"Other\" product type") @Nullable String description
) {}
