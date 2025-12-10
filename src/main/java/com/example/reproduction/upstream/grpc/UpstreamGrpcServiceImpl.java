package com.example.reproduction.upstream.grpc;

import com.example.reproduction.upstream.service.UpstreamService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UpstreamGrpcServiceImpl extends UpstreamServiceGrpc.UpstreamServiceImplBase {
    
    private final UpstreamService upstreamService;
    
    @Override
    public void process(ProcessRequest request, StreamObserver<ProcessResponse> responseObserver) {
        log.info("Upstream gRPC service received request, id: {}", request.getId());
        try {
            ProcessResponse response = upstreamService.process(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error processing upstream request", e);
            responseObserver.onError(e);
        }
    }
}

