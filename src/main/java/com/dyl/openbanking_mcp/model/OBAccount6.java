package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record OBAccount6(
	@JsonProperty("AccountId") @JsonPropertyDescription("A unique and immutable identifier used to identify the account resource") String accountId,
	@JsonProperty("Status") @JsonPropertyDescription("Specifies the status of account resource in code form") @Nullable String status,
	@JsonProperty("Currency") @JsonPropertyDescription("Identification of the currency in which the account is held") String currency,
	@JsonProperty("AccountCategory") @JsonPropertyDescription("Specifies the type of account (personal or business)") @Nullable String accountCategory,
	@JsonProperty("AccountTypeCode") @JsonPropertyDescription("Specifies the sub type of account (product family group)") @Nullable String accountTypeCode,
	@JsonProperty("Description") @JsonPropertyDescription("Specifies the description of the account type") @Nullable String description,
	@JsonProperty("Nickname") @JsonPropertyDescription("The nickname of the account, assigned by the account owner") @Nullable String nickname,
	@JsonProperty("OpeningDate") @JsonPropertyDescription("Date on which the account and related basic services are effectively operational for the account owner") @Nullable String openingDate,
	@JsonProperty("MaturityDate") @JsonPropertyDescription("Maturity date of the account") @Nullable String maturityDate,
	@JsonProperty("SwitchStatus") @JsonPropertyDescription("Specifies the switch status for the account, in a coded form") @Nullable String switchStatus,
	@JsonProperty("Account") @JsonPropertyDescription("Provides the details to identify an account") @Nullable List<OBAccountIdentifier> account,
	@JsonProperty("Servicer") @JsonPropertyDescription("Party that manages the account on behalf of the account owner") @Nullable OBServicer servicer
) {}
