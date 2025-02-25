package moneytransferapp.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    public MoneyTransferWorkFlowModel(String workflowId, String runId, String transactionReference, String fromAccount, String toAccount, int amountToTransfer, TransactionStatus status) {
        this.workflowId = workflowId;
        this.runId = runId;
        this.transactionReference = transactionReference;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amountToTransfer = amountToTransfer;
        this.status = status;
    }

}

