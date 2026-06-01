package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record OBReadProduct2(
	@JsonProperty("Data") @JsonPropertyDescription("Data container for product information") Data data,
	@JsonProperty("Links") @JsonPropertyDescription("Links relevant to the payload") @Nullable Links links,
	@JsonProperty("Meta") @JsonPropertyDescription("Meta Data relevant to the payload") @Nullable Meta meta
) {
	public record Data(
		@JsonProperty("Product") @JsonPropertyDescription("Product details associated with the Account") List<OBProduct2> product
	) {}
}
