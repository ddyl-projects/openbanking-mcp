package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBBankTransactionCode(
	@JsonProperty("Code") @JsonPropertyDescription("Specifies the family within a domain") @Nullable String code,
	@JsonProperty("SubCode") @JsonPropertyDescription("Specifies the sub-product family within a specific family") @Nullable String subCode
) {}
