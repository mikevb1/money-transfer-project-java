package moneytransferapp;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import moneytransferapp.temporal.AccountActivityImpl;
import moneytransferapp.temporal.MoneyTransferWorkflowImpl;
import moneytransferapp.temporal.Shared;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class TransferApp {

    @Autowired
    private Environment env;

    @Value("temporal.taskQueue")
    private String taskQueue;

    @Value("temporal.service.address")
    private String serviceAddress ;

    public static void main(String[] args) {
        SpringApplication.run(TransferApp.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startTemporalWorker() {
        // Create a stub that accesses a Temporal Service on the local development
        // machine
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

        WorkflowClient client = WorkflowClient.newInstance(serviceStub);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(taskQueue);

        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
        worker.registerActivitiesImplementations(new AccountActivityImpl());

        System.out.println("Worker is running and actively polling the Task Queue.");
        factory.start();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String urls = env.getProperty("cors.urls");
                CorsRegistration reg = registry.addMapping("/api/**");
                for(String url: urls.split(",")) {
                    reg.allowedOrigins(url);
                }
                System.out.println("CORS ORIGINS: " + urls.toString());
            }
        };
    }
}