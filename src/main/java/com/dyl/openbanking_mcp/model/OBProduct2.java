package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBProduct2(
	@JsonProperty("AccountId") @JsonPropertyDescription("A unique and immutable identifier used to identify the account resource") String accountId,
	@JsonProperty("ProductId") @JsonPropertyDescription("Identifier within the parent organisation for the product") @Nullable String productId,
	@JsonProperty("ProductName") @JsonPropertyDescription("The name of the Product used for marketing purposes from a customer perspective") @Nullable String productName,
	@JsonProperty("ProductType") @JsonPropertyDescription("Descriptive code for the product category") @Nullable String productType,
	@JsonProperty("SecondaryProductId") @JsonPropertyDescription("Any secondary Identification which supports Product Identifier to uniquely identify the current account banking products") @Nullable String secondaryProductId,
	@JsonProperty("MarketingStateId") @JsonPropertyDescription("Unique and unambiguous identification of a Product Marketing State") @Nullable String marketingStateId,
	@JsonProperty("OtherProductType") @JsonPropertyDescription("This field provides extension to the ProductType enumeration") @Nullable OBOtherProductType otherProductType
) {}
