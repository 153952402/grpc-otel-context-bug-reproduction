package com.example.reproduction.downstream.grpc;

import com.example.reproduction.downstream.service.DownstreamService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DownstreamGrpcServiceImpl extends DownstreamServiceGrpc.DownstreamServiceImplBase {
    
    private final DownstreamService downstreamService;
    
    @Override
    public void handle(HandleRequest request, StreamObserver<HandleResponse> responseObserver) {
        log.info("Downstream gRPC service received request, id: {}", request.getId());
        try {
            HandleResponse response = downstreamService.handle(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error handling downstream request", e);
            responseObserver.onError(e);
        }
    }
}

