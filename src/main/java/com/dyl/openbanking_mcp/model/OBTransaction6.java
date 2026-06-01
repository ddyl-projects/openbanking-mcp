package com.dyl.openbanking_mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

public record OBTransaction6(
	@JsonProperty("AccountId") @JsonPropertyDescription("A unique and immutable identifier used to identify the account resource") String accountId,
	@JsonProperty("TransactionId") @JsonPropertyDescription("Unique identifier for the transaction within a servicing institution") @Nullable String transactionId,
	@JsonProperty("TransactionReference") @JsonPropertyDescription("Unique reference for the transaction") @Nullable String transactionReference,
	@JsonProperty("CreditDebitIndicator") @JsonPropertyDescription("Indicates whether the transaction is a credit or a debit entry") String creditDebitIndicator,
	@JsonProperty("Status") @JsonPropertyDescription("Status of a transaction entry on the books of the account servicer") String status,
	@JsonProperty("BookingDateTime") @JsonPropertyDescription("Date and time when a transaction entry is posted to an account on the account servicer's books") String bookingDateTime,
	@JsonProperty("ValueDateTime") @JsonPropertyDescription("Date and time at which assets become available to the account owner in case of a credit entry, or cease to be available in case of a debit entry") @Nullable String valueDateTime,
	@JsonProperty("TransactionInformation") @JsonPropertyDescription("Further details of the transaction") @Nullable String transactionInformation,
	@JsonProperty("Amount") @JsonPropertyDescription("Amount of money in the cash transaction entry") OBAmount amount,
	@JsonProperty("ChargeAmount") @JsonPropertyDescription("Transaction charges to be paid by the charge bearer") @Nullable OBAmount chargeAmount,
	@JsonProperty("BankTransactionCode") @JsonPropertyDescription("Set of elements used to fully identify the type of underlying transaction resulting in an entry") @Nullable OBBankTransactionCode bankTransactionCode,
	@JsonProperty("ProprietaryBankTransactionCode") @JsonPropertyDescription("Set of elements to fully identify a proprietary bank transaction code") @Nullable OBProprietaryBankTransactionCode proprietaryBankTransactionCode,
	@JsonProperty("MerchantDetails") @JsonPropertyDescription("Details of the merchant involved in the transaction") @Nullable OBMerchantDetails merchantDetails,
	@JsonProperty("CreditorAgent") @JsonPropertyDescription("Financial institution servicing an account for the creditor") @Nullable OBAgent creditorAgent,
	@JsonProperty("DebtorAgent") @JsonPropertyDescription("Financial institution servicing an account for the debtor") @Nullable OBAgent debtorAgent,
	@JsonProperty("CreditorAccount") @JsonPropertyDescription("Unambiguous identification of the account of the creditor, in the case of a debit transaction") @Nullable OBCashAccount creditorAccount,
	@JsonProperty("DebtorAccount") @JsonPropertyDescription("Unambiguous identification of the account of the debtor, in the case of a credit transaction") @Nullable OBCashAccount debtorAccount
) {}
