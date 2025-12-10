package com.example.reproduction.downstream.service;

import com.example.reproduction.client.UpstreamClient;
import com.example.reproduction.downstream.grpc.HandleRequest;
import com.example.reproduction.downstream.grpc.HandleResponse;
import com.example.reproduction.event.ProcessEvent;
import com.example.reproduction.upstream.grpc.ProcessRequest;
import com.google.common.eventbus.AsyncEventBus;
import io.grpc.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownstreamServiceImpl implements DownstreamService {
    
    private final UpstreamClient upstreamClient;
    private final AsyncEventBus asyncEventBus;
    
    @Override
    public HandleResponse handle(HandleRequest request) {
        log.info("Current gRPC Context in downstream service: {}", Context.current());
        
        // First call to upstream service (synchronous, should work fine)
        ProcessRequest upstreamRequest = ProcessRequest.newBuilder()
                .setId(request.getId())
                .setData("First call: " + request.getData())
                .build();
        
        var firstResponse = upstreamClient.process(upstreamRequest);
        log.info("First upstream call succeeded: {}", firstResponse.getSuccess());
        
        // Create event and post to async event bus
        // This will be processed in an async thread where gRPC Context is incorrectly propagated
        ProcessEvent event = new ProcessEvent(request.getId(), request.getData());
        log.info("Posting event to async event bus, current gRPC Context: {}", Context.current());
        asyncEventBus.post(event);
        
        return HandleResponse.newBuilder()
                .setSuccess(true)
                .build();
    }
}

