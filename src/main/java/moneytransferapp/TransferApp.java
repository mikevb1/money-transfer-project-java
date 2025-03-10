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
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class TransferApp {

    private WorkerFactory factory;
    private WorkflowServiceStubs serviceStub;

    public static void main(String[] args) {
        SpringApplication.run(TransferApp.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startTemporalWorker() {
        // Create a stub that accesses a Temporal Service on the local development
        // machine
        this.serviceStub = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(serviceStub);
        this.factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(Shared.MONEY_TRANSFER_TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
        worker.registerActivitiesImplementations(new AccountActivityImpl());

        System.out.println("Worker is running and actively polling the Task Queue.");
        factory.start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stopTemporalWorker() {
        System.out.println("Shutting down Temporal worker...");

        if (factory != null) {
            factory.shutdown();

        }
        if (serviceStub != null) {
            serviceStub.shutdownNow();
        }
        System.out.println("Temporal worker shut down successfully.");
    }
}