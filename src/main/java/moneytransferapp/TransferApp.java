package moneytransferapp;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import moneytransferapp.temporal.AccountActivityImpl;
import moneytransferapp.temporal.MoneyTransferWorkflowImpl;
import moneytransferapp.temporal.Shared;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class TransferApp {

    public static void main(String[] args) {
        SpringApplication.run(TransferApp.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startTemporalWorker() {
        // Create a stub that accesses a Temporal Service on the local development
        // machine
        WorkflowServiceStubs serviceStub = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(serviceStub);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(Shared.MONEY_TRANSFER_TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
        worker.registerActivitiesImplementations(new AccountActivityImpl());

        System.out.println("Worker is running and actively polling the Task Queue.");
        factory.start();
    }
}