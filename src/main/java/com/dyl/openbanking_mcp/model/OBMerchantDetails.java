package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBMerchantDetails(
	@JsonProperty("MerchantName") @JsonPropertyDescription("Name by which the merchant is known") @Nullable String merchantName,
	@JsonProperty("MerchantCategoryCode") @JsonPropertyDescription("Category code conform to ISO 18245, related to the type of services or goods the merchant provides for the transaction") @Nullable String merchantCategoryCode
) {}
