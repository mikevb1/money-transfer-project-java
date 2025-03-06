// @@@SNIPSTART money-transfer-java-worker
package moneytransferapp.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

public class MoneyTransferWorker {
    @Value("temporal.taskQueue")
    private static String taskQueue;

    @Value("temporal.service.address")
    private static String serviceAddress;

    public static void main(String[] args) {

        // Spring context om application.properties in te lezen
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.refresh();
        Environment env = context.getEnvironment();

        // Extern Temporal service adres ophalen uit properties
        String temporalAddress = env.getProperty("temporal.service.address");

        // Gebruik het externe adres voor de serviceStub configuratie
        WorkflowServiceStubs serviceStub = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalAddress)
                        .build()
        );

        // De rest blijft hetzelfde
        WorkflowClient client = WorkflowClient.newInstance(serviceStub);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(env.getProperty("temporal.taskQueue", "money-transfer-task-queue"));
        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
        worker.registerActivitiesImplementations(new AccountActivityImpl());

        System.out.println("Worker is running and actively polling the Task Queue.");
        factory.start();



//        // Create a stub that accesses a Temporal Service on the local development
//        // machine
//        WorkflowServiceStubs serviceStub = WorkflowServiceStubs.newLocalServiceStubs();
//
//        // The Worker uses the Client to communicate with the Temporal Service
//        WorkflowClient client = WorkflowClient.newInstance(serviceStub);
//
//        // A WorkerFactory creates Workers
//        WorkerFactory factory = WorkerFactory.newInstance(client);
//
//        // A Worker listens to one Task Queue.
//        // This Worker processes both Workflows and Activities
//        Worker worker = factory.newWorker(Shared.MONEY_TRANSFER_TASK_QUEUE);
//
//        // Register a Workflow implementation with this Worker
//        // The implementation must be known at runtime to dispatch Workflow tasks
//        // Workflows are stateful so a type is needed to create instances.
//        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
//
//        // Register Activity implementation(s) with this Worker.
//        // The implementation must be known at runtime to dispatch Activity tasks
//        // Activities are stateless and thread safe so a shared instance is used.
//        worker.registerActivitiesImplementations(new AccountActivityImpl());
//
//        System.out.println("Worker is running and actively polling the Task Queue.");
//        System.out.println("To quit, use ^C to interrupt.");
//
//        // Start all registered Workers. The Workers will start polling the Task Queue.
//        factory.start();
    }
}
// @@@SNIPEND


