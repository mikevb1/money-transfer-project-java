package moneytransferapp.config;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    @Bean
    public WorkflowClient workflowClient() {
        // Create a service stub (this will connect to your Temporal service)
        WorkflowServiceStubs serviceStubs = WorkflowServiceStubs.newServiceStubs( WorkflowServiceStubsOptions.newBuilder()
                .setTarget("localhost:7233")
                .build()
        );

        // Create a WorkflowClient to interact with Temporal
        return WorkflowClient.newInstance(serviceStubs);
    }
}
