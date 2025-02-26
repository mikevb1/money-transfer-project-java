package moneytransferapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MoneyTransferWorkFlowModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String workflowId;
    private String runId;
    private String transactionReference;
    private String fromAccount;
    private String toAccount;
    private int amountToTransfer;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    public MoneyTransferWorkFlowModel(String workflowId, String runId, String transactionReference, String fromAccount, String toAccount, int amountToTransfer, TransactionStatus status, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.workflowId = workflowId;
        this.runId = runId;
        this.transactionReference = transactionReference;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amountToTransfer = amountToTransfer;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

}

