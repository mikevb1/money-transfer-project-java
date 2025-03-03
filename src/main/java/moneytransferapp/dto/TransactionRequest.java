package moneytransferapp.dto;

public record TransactionRequest(String fromAccount, String toAccount, double amountToTransfer) {
}
