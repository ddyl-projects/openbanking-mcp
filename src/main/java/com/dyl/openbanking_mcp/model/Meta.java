package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record Meta(
	@JsonProperty("TotalPages") @JsonPropertyDescription("Total number of pages available") @Nullable Integer totalPages,
	@JsonProperty("FirstAvailableDateTime") @JsonPropertyDescription("First available date and time for the data") @Nullable String firstAvailableDateTime,
	@JsonProperty("LastAvailableDateTime") @JsonPropertyDescription("Last available date and time for the data") @Nullable String lastAvailableDateTime
) {}
