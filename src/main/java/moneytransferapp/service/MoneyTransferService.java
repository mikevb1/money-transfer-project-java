package moneytransferapp.service;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import moneytransferapp.TransferApp;
import moneytransferapp.dto.TransactionRequest;
import moneytransferapp.model.MoneyTransferWorkFlowModel;
import moneytransferapp.model.TransactionStatus;
import moneytransferapp.temporal.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import moneytransferapp.repository.MoneyTransferRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MoneyTransferService {

    private final MoneyTransferRepository moneyTransferRepository;
    private final String taskQueue;
    private static final Integer MIN_AMOUNT_FOR_APPROVAL = 1000;

    @Autowired
    private WorkflowClient client;

    @Autowired
    public MoneyTransferService(
            MoneyTransferRepository moneyTransferRepository,
            WorkflowClient workflowClient,
            @Value("${temporal.taskQueue}") String taskQueue) {
        
        this.moneyTransferRepository = moneyTransferRepository;
        this.client = workflowClient;
        this.taskQueue = taskQueue;
    }

    public String startTransaction() {
        // Create the workflow options
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId(String.valueOf(UUID.randomUUID( )))
                .build();

        // Create the workflow stub
        MoneyTransferWorkflow workflow = client.newWorkflowStub(MoneyTransferWorkflow.class, options);

        // Generate random transaction details
        String referenceId = UUID.randomUUID().toString().substring(0, 18);
        String fromAccount = TransferApp.randomAccountIdentifier();
        String toAccount = TransferApp.randomAccountIdentifier();
        double amountToTransfer = ThreadLocalRandom.current().nextInt(100, 5000);
        TransactionStatus status;
        if (amountToTransfer < MIN_AMOUNT_FOR_APPROVAL) {
            status = TransactionStatus.IN_PROGRESS;
        } else {
            status = TransactionStatus.PENDING;
        }
        TransactionDetails transaction = new CoreTransactionDetails(fromAccount, toAccount, referenceId,
                amountToTransfer);

        // Start the workflow
        WorkflowExecution we = WorkflowClient.start(workflow::transfer, transaction);

        // Create the response map
        Map<String, String> response = new HashMap<>();
        response.put("workflowId", we.getWorkflowId());
        response.put("runId", we.getRunId());
        response.put("transactionReference", referenceId);

        LocalDateTime createdAt = LocalDateTime.now();

        // Save the workflow information to the database
        MoneyTransferWorkFlowModel moneyTransferWorkflowEntity = new MoneyTransferWorkFlowModel(
                we.getWorkflowId(),
                we.getRunId(),
                referenceId,
                fromAccount,
                toAccount,
                amountToTransfer,
                status,
                createdAt,
                null);

        moneyTransferRepository.save(moneyTransferWorkflowEntity);

        if (amountToTransfer >= 1000) {
            return String.format(
                    "Request received. Because your request of €%.2f is higher than €1000, it needs to be reviewed. Thank you for your patience.",
                    amountToTransfer);
        } else {
            moneyTransferWorkflowEntity.setStatus(TransactionStatus.APPROVED);
            moneyTransferWorkflowEntity.setProcessedAt(LocalDateTime.now());
            moneyTransferRepository.save(moneyTransferWorkflowEntity);
            return String.format("Amount of €%.2f transferred.", amountToTransfer);
        }
    }

    public List<MoneyTransferWorkFlowModel> getTransactions() {
        return moneyTransferRepository.findAll();
    }

    public String approveTransaction(String transactionReference) {
        Optional<MoneyTransferWorkFlowModel> transactionOpt = moneyTransferRepository
                .findByTransactionReference(transactionReference);

        System.out.println("Transaction: " + transactionOpt);

        if (transactionOpt.isPresent()) {
            MoneyTransferWorkFlowModel transaction = transactionOpt.get();
            String workflowId = transaction.getWorkflowId();

            if (workflowId == null || workflowId.isEmpty()) {
                return "Error: Workflow ID is missing for transaction " + transactionReference;
            }

            System.out.println("Workflow ID: " + workflowId);

            try {

                MoneyTransferWorkflow workflow = client.newWorkflowStub(MoneyTransferWorkflow.class, workflowId);

                // Try to approve the transaction, if the workflow is active
                workflow.approveTransaction(mapToTransactionDetails(transaction));
                transaction.setStatus(TransactionStatus.APPROVED);
                transaction.setProcessedAt(LocalDateTime.now());
                moneyTransferRepository.save(transaction);
                return "Transaction " + transactionReference + " manually approved.";

            } catch (Exception e) {
                // Handle the case where the workflow is not active anymore
                System.out.println("Error: Workflow with ID " + workflowId + " is not active or already completed.");
                return "Error: Workflow with ID " + workflowId + " is not active or has already been completed.";
            }
        } else {
            return "Transaction with reference " + transactionReference + " not found.";
        }
    }

    public String disapproveTransaction(String transactionReference) {
        Optional<MoneyTransferWorkFlowModel> transactionOpt = moneyTransferRepository
                .findByTransactionReference(transactionReference);

        System.out.println("Transaction: " + transactionOpt);

        if (transactionOpt.isPresent()) {
            MoneyTransferWorkFlowModel transaction = transactionOpt.get();
            String workflowId = transaction.getWorkflowId();

            if (workflowId == null || workflowId.isEmpty()) {
                return "Error: Workflow ID is missing for transaction " + transactionReference;
            }

            System.out.println("Workflow ID: " + workflowId);

            try {

                MoneyTransferWorkflow workflow = client.newWorkflowStub(MoneyTransferWorkflow.class, workflowId);

                // Try to approve the transaction, if the workflow is active
                workflow.disapproveTransaction(mapToTransactionDetails(transaction));
                transaction.setStatus(TransactionStatus.DECLINED);
                transaction.setProcessedAt(LocalDateTime.now());
                moneyTransferRepository.save(transaction);
                return "Transaction " + transactionReference + " manually disapproved. Starting refund";

            } catch (Exception e) {
                // Handle the case where the workflow is not active anymore
                System.out.println("Error: Workflow with ID " + workflowId + " is not active or already completed.");
                return "Error: Workflow with ID " + workflowId + " is not active or has already been completed.";
            }
        } else {
            return "Transaction with reference " + transactionReference + " not found.";
        }
    }

    public String requestTransfer(TransactionRequest request) {

        // Create the workflow options
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId(String.valueOf(UUID.randomUUID()))
                .build();

        // Create the workflow stub
        MoneyTransferWorkflow workflow = client.newWorkflowStub(MoneyTransferWorkflow.class, options);

        // Generate random transaction details
        String referenceId = UUID.randomUUID().toString().substring(0, 18);
        String fromAccount = request.fromAccount();
        String toAccount = request.toAccount();
        double amountToTransfer = request.amountToTransfer();

        TransactionStatus status;

        if (amountToTransfer < MIN_AMOUNT_FOR_APPROVAL) {
            status = TransactionStatus.IN_PROGRESS;
        } else {
            status = TransactionStatus.PENDING;
        }

        TransactionDetails transaction = new CoreTransactionDetails(fromAccount, toAccount, referenceId,
                amountToTransfer);

        // Start the workflow
        WorkflowExecution we = WorkflowClient.start(workflow::transfer, transaction);

        // Create the response map
        Map<String, String> response = new HashMap<>();
        response.put("workflowId", we.getWorkflowId());
        response.put("runId", we.getRunId());
        response.put("transactionReference", referenceId);

        LocalDateTime createdAt = LocalDateTime.now();

        // Save the workflow information to the database
        MoneyTransferWorkFlowModel moneyTransferWorkflowEntity = new MoneyTransferWorkFlowModel(
                we.getWorkflowId(),
                we.getRunId(),
                referenceId,
                fromAccount,
                toAccount,
                amountToTransfer,
                status,
                createdAt,
                null);

        moneyTransferRepository.save(moneyTransferWorkflowEntity);

        if (amountToTransfer >= 1000) {
            return String.format(
                    "Request received. Because your request of €%.2f is higher than €1000, it needs to be reviewed. Thank you for your patience.",
                    amountToTransfer);
        } else {
            moneyTransferWorkflowEntity.setStatus(TransactionStatus.APPROVED);
            moneyTransferWorkflowEntity.setProcessedAt(LocalDateTime.now());
            moneyTransferRepository.save(moneyTransferWorkflowEntity);
            return String.format("Amount of €%.2f transferred.", amountToTransfer);
        }
    }

    public TransactionDetails mapToTransactionDetails(MoneyTransferWorkFlowModel workflowModel) {
        return new CoreTransactionDetails(
                workflowModel.getFromAccount(), // sourceAccountId
                workflowModel.getToAccount(), // destinationAccountId
                workflowModel.getTransactionReference(), // transactionReferenceId
                workflowModel.getAmountToTransfer() // amountToTransfer
        );
    }

    public void updateTransactionStatusInDatabase(String transactionReference, TransactionStatus status) {
        Optional<MoneyTransferWorkFlowModel> transactionOpt = moneyTransferRepository
                .findByTransactionReference(transactionReference);

        if (transactionOpt.isPresent()) {
            MoneyTransferWorkFlowModel transaction = transactionOpt.get();
            transaction.setStatus(status); // Set the new status (approved or declined)
            moneyTransferRepository.save(transaction); // Save the updated status
        }
    }

}
