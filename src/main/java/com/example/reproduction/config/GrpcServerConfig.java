package com.example.reproduction.config;

import com.example.reproduction.upstream.grpc.UpstreamGrpcServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

/**
 * Configuration for running two gRPC servers on different ports.
 * The downstream service runs on port 9090 (configured via spring-boot-starter-grpc).
 * The upstream service runs on port 9091 (manually configured here).
 */
@Slf4j
@Configuration
public class GrpcServerConfig {
    
    @Value("${upstream.grpc.port:9091}")
    private int upstreamPort;
    
    private Server upstreamServer;
    
    private final UpstreamGrpcServiceImpl upstreamService;
    
    public GrpcServerConfig(UpstreamGrpcServiceImpl upstreamService) {
        this.upstreamService = upstreamService;
    }
    
    @PostConstruct
    public void startUpstreamServer() throws IOException {
        upstreamServer = ServerBuilder.forPort(upstreamPort)
                .addService(upstreamService)
                .build();
        upstreamServer.start();
        log.info("Upstream gRPC server started on port {}", upstreamPort);
    }
    
    @PreDestroy
    public void stopUpstreamServer() {
        if (upstreamServer != null && !upstreamServer.isShutdown()) {
            upstreamServer.shutdown();
            log.info("Upstream gRPC server stopped");
        }
    }
}

