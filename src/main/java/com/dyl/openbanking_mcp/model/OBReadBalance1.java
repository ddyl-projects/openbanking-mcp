package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record OBReadBalance1(
	@JsonProperty("Data") @JsonPropertyDescription("Data container for balance information") Data data,
	@JsonProperty("Links") @JsonPropertyDescription("Links relevant to the payload") @Nullable Links links,
	@JsonProperty("Meta") @JsonPropertyDescription("Meta Data relevant to the payload") @Nullable Meta meta
) {
	public record Data(
		@JsonProperty("Balance") @JsonPropertyDescription("Set of elements used to define the balance details") List<OBBalance1> balance
	) {}
}
