package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBCreditLine(
	@JsonProperty("Included") @JsonPropertyDescription("Indicates whether or not the credit line is included in the balance of the account") Boolean included,
	@JsonProperty("Type") @JsonPropertyDescription("Limit type, in a coded form") @Nullable String type,
	@JsonProperty("Amount") @JsonPropertyDescription("Amount of money of the credit line") @Nullable OBAmount amount
) {}
