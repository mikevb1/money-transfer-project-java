package moneytransferapp.config;

import io.grpc.ClientInterceptor;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TemporalConfig {
    private static final Logger logger = LoggerFactory.getLogger(TemporalConfig.class);

    @Value("${temporal.service.address}")
    private String temporalServiceAddress;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        logger.info("Initializing Temporal connection to: {}", temporalServiceAddress);

        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalServiceAddress)
                .setRpcTimeout(Duration.ofSeconds(20))
                .setRpcLongPollTimeout(Duration.ofSeconds(70))
                .setConnectionBackoffResetFrequency(Duration.ofSeconds(20))
                .setGrpcReconnectFrequency(Duration.ofSeconds(10))
                .setKeepAliveTime(Duration.ofSeconds(30))
                .setKeepAliveTimeout(Duration.ofSeconds(20))
                .setRpcQueryTimeout(Duration.ofSeconds(20))
                .build();

        try {
            return WorkflowServiceStubs.newServiceStubs(options);
        } catch (Exception e) {
            logger.error("Failed to create Temporal service stubs", e);
            throw e;
        }
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {
        try {
            return WorkflowClient.newInstance(workflowServiceStubs);
        } catch (Exception e) {
            logger.error("Failed to create Temporal workflow client", e);
            throw e;
        }
    }
}
