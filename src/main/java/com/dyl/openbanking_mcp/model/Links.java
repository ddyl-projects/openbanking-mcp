package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record Links(
	@JsonProperty("Self") @JsonPropertyDescription("Absolute URI to the current page of results") @Nullable String self,
	@JsonProperty("First") @JsonPropertyDescription("Absolute URI to the first page of results") @Nullable String first,
	@JsonProperty("Prev") @JsonPropertyDescription("Absolute URI to the previous page of results") @Nullable String prev,
	@JsonProperty("Next") @JsonPropertyDescription("Absolute URI to the next page of results") @Nullable String next,
	@JsonProperty("Last") @JsonPropertyDescription("Absolute URI to the last page of results") @Nullable String last
) {}
