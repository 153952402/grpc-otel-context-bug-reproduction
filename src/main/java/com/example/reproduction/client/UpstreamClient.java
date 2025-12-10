package com.example.reproduction.client;

import com.example.reproduction.upstream.grpc.ProcessRequest;
import com.example.reproduction.upstream.grpc.ProcessResponse;
import com.example.reproduction.upstream.grpc.UpstreamServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
@Component
public class UpstreamClient {
    
    @Value("${upstream.grpc.port:9091}")
    private int upstreamPort;
    
    private ManagedChannel channel;
    private UpstreamServiceGrpc.UpstreamServiceBlockingStub blockingStub;
    
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress("localhost", upstreamPort)
                .usePlaintext()
                .build();
        blockingStub = UpstreamServiceGrpc.newBlockingStub(channel);
        log.info("UpstreamClient initialized, connecting to localhost:{}", upstreamPort);
    }
    
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
    
    public ProcessResponse process(ProcessRequest request) {
        log.info("UpstreamClient.process called with id: {}", request.getId());
        try {
            ProcessResponse response = blockingStub.process(request);
            log.info("UpstreamClient.process response: success={}", response.getSuccess());
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call upstream service", e);
        }
    }
}

