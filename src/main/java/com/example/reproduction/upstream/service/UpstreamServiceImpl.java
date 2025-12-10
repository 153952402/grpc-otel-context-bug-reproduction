package com.example.reproduction.upstream.service;

import com.example.reproduction.upstream.grpc.ProcessRequest;
import com.example.reproduction.upstream.grpc.ProcessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UpstreamServiceImpl implements UpstreamService {
    
    @Override
    public ProcessResponse process(ProcessRequest request) {
        log.info("UpstreamService.process called with id: {}, data: {}", request.getId(), request.getData());
        return ProcessResponse.newBuilder()
                .setSuccess(true)
                .setResult("Processed: " + request.getData())
                .build();
    }
}

