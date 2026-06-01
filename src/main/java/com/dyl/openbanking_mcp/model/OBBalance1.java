package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record OBBalance1(
	@JsonProperty("AccountId") @JsonPropertyDescription("A unique and immutable identifier used to identify the account resource") String accountId,
	@JsonProperty("CreditDebitIndicator") @JsonPropertyDescription("Indicates whether the balance is a credit or a debit balance") String creditDebitIndicator,
	@JsonProperty("Type") @JsonPropertyDescription("Balance type, in a coded form") String type,
	@JsonProperty("DateTime") @JsonPropertyDescription("Indicates the date and time of the balance") String dateTime,
	@JsonProperty("Amount") @JsonPropertyDescription("Amount of money of the cash balance") OBBalanceAmount amount,
	@JsonProperty("CreditLine") @JsonPropertyDescription("Set of elements used to provide details on the credit line") @Nullable List<OBCreditLine> creditLine
) {}
