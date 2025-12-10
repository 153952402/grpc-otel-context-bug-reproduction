package com.example.reproduction.downstream.service;

import com.example.reproduction.downstream.grpc.HandleRequest;
import com.example.reproduction.downstream.grpc.HandleResponse;

public interface DownstreamService {
    HandleResponse handle(HandleRequest request);
}

