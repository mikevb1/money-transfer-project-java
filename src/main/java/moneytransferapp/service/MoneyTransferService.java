package moneytransferapp.service;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import moneytransferapp.model.MoneyTransferWorkFlowModel;
import moneytransferapp.model.TransactionStatus;
import moneytransferapp.temporal.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import moneytransferapp.repository.MoneyTransferRepository;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MoneyTransferService {

    private final MoneyTransferRepository moneyTransferRepository;
    private final WorkflowClient client;

    @Autowired
    public MoneyTransferService(WorkflowClient client, MoneyTransferRepository moneyTransferRepository) {
        this.moneyTransferRepository = moneyTransferRepository;


        WorkflowServiceStubs serviceStubs = WorkflowServiceStubs.newLocalServiceStubs();

        this.client = WorkflowClient.newInstance(serviceStubs);

        startWorker();
    }


    private void startWorker() {
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(Shared.MONEY_TRANSFER_TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
        worker.registerActivitiesImplementations(new AccountActivityImpl());

        System.out.println("Worker started, listening to task queue: " + Shared.MONEY_TRANSFER_TASK_QUEUE);
        factory.start();
    }

    public Map<String, String> startTransaction() {
        // Create the workflow options
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(Shared.MONEY_TRANSFER_TASK_QUEUE)
                .setWorkflowId(String.valueOf(UUID.randomUUID()))
                .build();

        // Create the workflow stub
        MoneyTransferWorkflow workflow = client.newWorkflowStub(MoneyTransferWorkflow.class, options);

        // Generate random transaction details
        String referenceId = UUID.randomUUID().toString().substring(0, 18);
        String fromAccount = TransferApp.randomAccountIdentifier();
        String toAccount = TransferApp.randomAccountIdentifier();
        int amountToTransfer = ThreadLocalRandom.current().nextInt(100, 5000);
        TransactionStatus status = TransactionStatus.IN_PROGRESS;
        TransactionDetails transaction = new CoreTransactionDetails(fromAccount, toAccount, referenceId, amountToTransfer);

        // Start the workflow
        WorkflowExecution we = WorkflowClient.start(workflow::transfer, transaction);

        // Create the response map
        Map<String, String> response = new HashMap<>();
        response.put("workflowId", we.getWorkflowId());
        response.put("runId", we.getRunId());
        response.put("transactionReference", referenceId);

        // Save the workflow information to the database
        MoneyTransferWorkFlowModel moneyTransferWorkflowEntity = new MoneyTransferWorkFlowModel(
                we.getWorkflowId(),
                we.getRunId(),
                referenceId,
                fromAccount,
                toAccount,
                amountToTransfer,
                status
        );

        moneyTransferRepository.save(moneyTransferWorkflowEntity);

        return response;
    }

    public List<MoneyTransferWorkFlowModel> getTransactions() {
        return moneyTransferRepository.findAll();
    }


    public String approveTransaction(String transactionReference) {
        Optional<MoneyTransferWorkFlowModel> transactionOpt = moneyTransferRepository.findByTransactionReference(transactionReference);

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
                transaction.setStatus(TransactionStatus.COMPLETED);
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
        Optional<MoneyTransferWorkFlowModel> transactionOpt = moneyTransferRepository.findByTransactionReference(transactionReference);

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

        public TransactionDetails mapToTransactionDetails (MoneyTransferWorkFlowModel workflowModel){
            return new CoreTransactionDetails(
                    workflowModel.getFromAccount(),       // sourceAccountId
                    workflowModel.getToAccount(),         // destinationAccountId
                    workflowModel.getTransactionReference(), // transactionReferenceId
                    workflowModel.getAmountToTransfer()   // amountToTransfer
            );
        }

        // Assuming you have a repository and a service

        public MoneyTransferWorkFlowModel getTransactionByReference (String transactionReference){
            return moneyTransferRepository.findByTransactionReference(transactionReference)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));
        }

    }
