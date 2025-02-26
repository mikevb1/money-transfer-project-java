package moneytransferapp.dto;

public record TransactionRequest(String fromAccount, String toAccount, int amountToTransfer) {
}
