package moneytransferapp.dto;

import moneytransferapp.model.TransactionStatus;

public record TransactionResponse(String workflowId, String runId, TransactionStatus status, String transactionReference, String fromAccount, String toAccount, Double amount) {
}
