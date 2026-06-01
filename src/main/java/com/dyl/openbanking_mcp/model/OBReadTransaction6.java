package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBReadTransaction6(
	@JsonProperty("Data") @JsonPropertyDescription("Data container for transaction information") Data data,
	@JsonProperty("Links") @JsonPropertyDescription("Links relevant to the payload") @Nullable Links links,
	@JsonProperty("Meta") @JsonPropertyDescription("Meta Data relevant to the payload") @Nullable Meta meta
) {
	public record Data(
		@JsonProperty("Transaction") @JsonPropertyDescription("List of transactions") java.util.List<OBTransaction6> transaction
	) {}
}
