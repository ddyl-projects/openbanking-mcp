package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBBalanceAmount(
	@JsonProperty("Amount") @JsonPropertyDescription("A number of monetary units specified in an active currency where the unit of currency is explicit and compliant with ISO 4217") String amount,
	@JsonProperty("Currency") @JsonPropertyDescription("A code allocated to a currency by a Maintenance Agency under an international identification scheme, as described in ISO 4217") String currency,
	@JsonProperty("SubType") @JsonPropertyDescription("Balance sub type, in a coded form") @Nullable String subType
) {}
