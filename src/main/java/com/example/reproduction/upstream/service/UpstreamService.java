package com.example.reproduction.upstream.service;

import com.example.reproduction.upstream.grpc.ProcessRequest;
import com.example.reproduction.upstream.grpc.ProcessResponse;

public interface UpstreamService {
    ProcessResponse process(ProcessRequest request);
}

