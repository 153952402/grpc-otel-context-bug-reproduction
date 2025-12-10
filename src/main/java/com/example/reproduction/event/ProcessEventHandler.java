package com.example.reproduction.event;

import com.example.reproduction.client.UpstreamClient;
import com.example.reproduction.upstream.grpc.ProcessRequest;
import com.google.common.eventbus.Subscribe;
import io.opentelemetry.context.ContextKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Event handler that processes events asynchronously.
 * This is where the bug manifests: gRPC context is incorrectly propagated here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessEventHandler {
    
    private final UpstreamClient upstreamClient;
    
    @PostConstruct
    public void register() {
        // This will be registered with EventBus by Spring
    }
    
    @Subscribe
    public void handle(ProcessEvent event) {
        // Check OpenTelemetry Context
        io.opentelemetry.context.Context otelContext = io.opentelemetry.context.Context.current();

        //!!! use evaluate expression inspect status : ((Context)(otelContext.get(((ArrayBasedContext) otelContext).entries[0]))).isCancelled()
        //!!! use evaluate expression inspect status : ((Context)(otelContext.get(((ArrayBasedContext) otelContext).entries[0]))).isCancelled()
        //!!! use evaluate expression inspect status : ((Context)(otelContext.get(((ArrayBasedContext) otelContext).entries[0]))).isCancelled()

        log.info("OpenTelemetry Context in async thread: {}", otelContext);
        
        // Second call to upstream service (in async thread)
        // This is where the error occurs because gRPC context is incorrectly propagated
        ProcessRequest upstreamRequest = ProcessRequest.newBuilder()
                .setId(event.getId())
                .setData("Second call (async): " + event.getData())
                .build();
        try {
            var response = upstreamClient.process(upstreamRequest);
            log.info("Second upstream call succeeded: {}", response.getSuccess());
        } catch (Exception e) {
            log.error("ERROR: Second upstream call failed!", e);
            log.error("This is the bug: gRPC context was incorrectly propagated to async thread");
            throw e;
        }
    }
}

