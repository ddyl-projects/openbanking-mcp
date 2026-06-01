package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBProprietaryBankTransactionCode(
	@JsonProperty("Code") @JsonPropertyDescription("Proprietary bank transaction code to identify the underlying transaction") @Nullable String code,
	@JsonProperty("Issuer") @JsonPropertyDescription("Identification of the issuer of the proprietary bank transaction code") @Nullable String issuer
) {}
