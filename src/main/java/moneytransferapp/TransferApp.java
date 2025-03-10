package moneytransferapp;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import moneytransferapp.temporal.AccountActivityImpl;
import moneytransferapp.temporal.MoneyTransferWorkflowImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
public class TransferApp {
    private static final SecureRandom random;

    @Autowired
    private Environment env;

    @Value("${temporal.taskQueue}")
    private String taskQueue;

    @Autowired
    private WorkflowClient workflowClient;

    @Value("${temporal.service.address}")
    private String temporalServiceAddress;

    private WorkerFactory factory;

    private WorkflowServiceStubs service;

    static {
        // Seed the random number generator with nano date
        random = new SecureRandom();
        random.setSeed(Instant.now().getNano());
    }

    // Utility method voor het genereren van random account nummers
    public static String randomAccountIdentifier() {
        return String.format("NL%s BANK %s %s %s",
                IntStream.range(0, 2).mapToObj(i -> String.valueOf(random.nextInt(10))).collect(Collectors.joining()),
                IntStream.range(0, 4).mapToObj(i -> String.valueOf(random.nextInt(10))).collect(Collectors.joining()),
                IntStream.range(0, 4).mapToObj(i -> String.valueOf(random.nextInt(10))).collect(Collectors.joining()),
                IntStream.range(0, 2).mapToObj(i -> String.valueOf(random.nextInt(10))).collect(Collectors.joining())
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(TransferApp.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startTemporalWorker() {
        System.out.printf("\nMONEY TRANSFER PROJECT\n\n");
        System.out.println("Connecting to Temporal service at: " + temporalServiceAddress);

        try {
            this.factory = WorkerFactory.newInstance(workflowClient);
            Worker worker = factory.newWorker(taskQueue);

            worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
            worker.registerActivitiesImplementations(new AccountActivityImpl());

            System.out.println("Worker is running and actively polling the Task Queue: " + taskQueue);
            factory.start();

        } catch (Exception e) {
            System.err.println("Failed to connect to Temporal service: " + e.getMessage());
            // Optioneel: als je wilt dat de applicatie stopt bij geen verbinding
            // System.exit(1);
        }
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String urls = env.getProperty("cors.urls");
                CorsRegistration reg = registry.addMapping("/api/**");
                if (urls != null) {
                    for (String url : urls.split(",")) {
                        reg.allowedOrigins(url);
                    }
                    System.out.println("CORS ORIGINS: " + urls);
                }
            }
        };
    }

    @EventListener(ContextClosedEvent.class)
    public void stopTemporalWorker() {
        System.out.println("Shutting down Temporal worker...");

        if (factory != null) {
            factory.shutdown();

        }
        if (workflowClient.getWorkflowServiceStubs() != null) {
            workflowClient.getWorkflowServiceStubs().shutdown();
        }
        System.out.println("Temporal worker shut down successfully.");
    }
}