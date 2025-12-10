# Quick Start Guide

## Prerequisites

- Java 21+
- Maven 3.6+
- grpcurl (for testing, optional)

## Quick Run

### 1. Start the Application

```bash
cd grpc-otel-context-bug-reproduction
mvn spring-boot:run
```

Or use the provided script:

```bash
./scripts/run.sh
```

After the application starts, you should see:
- Downstream gRPC service started on port 9090
- Upstream gRPC server started on port 9091

### 2. Test Issue Reproduction

In another terminal, use grpcurl to call the downstream service:

```bash
grpcurl -plaintext \
  -import-path ./src/main/resources/proto \
  -proto downstream_service.proto \
  -d '{"id":"test-123","data":"test data"}' \
  127.0.0.1:9090 \
  com.example.reproduction.downstream.grpc.DownstreamService/Handle
```

Or use the provided test script:

```bash
./scripts/test.sh
```

### 3. Observe Log Output

Check the application logs, you should see:

1. **Downstream service receives request**
2. **First call to upstream service (synchronous, successful)**
3. **Event sent to async thread**
4. **gRPC Context detected in async thread (should NOT be ROOT - this is the bug)**
5. **Second call to upstream service (async, may fail)**

## Expected Behavior

Normally, in async threads:
- gRPC Context should be `ROOT` (because async threads should not have the gRPC request Context)

Due to the bug, the actual situation is:
- gRPC Context is NOT `ROOT`, but propagated from the main thread
- This may cause errors when making the second call to the upstream service

## Issue Verification

Check if the following output appears in the logs:
- `Is gRPC Context ROOT? false` (this proves the bug exists)
- `ERROR: Second upstream call failed!` (if the bug causes the call to fail)

## Stop the Application

Press `Ctrl+C` to stop the application.

