package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBAccountIdentifier(
	@JsonProperty("SchemeName") @JsonPropertyDescription("Name of the identification scheme, in a coded form as published in an external list") String schemeName,
	@JsonProperty("Identification") @JsonPropertyDescription("Identification assigned by an institution to identify an account") String identification,
	@JsonProperty("Name") @JsonPropertyDescription("The account name is the name or names of the account owner(s) represented at an account level") @Nullable String name,
	@JsonProperty("LEI") @JsonPropertyDescription("Legal entity identification as an alternate identification for a party") @Nullable String lei,
	@JsonProperty("SecondaryIdentification") @JsonPropertyDescription("Secondary identification of the account, as assigned by the account servicing institution") @Nullable String secondaryIdentification
) {}
