# gRPC OpenTelemetry Context Bug Reproduction

This is a minimal reproduction project to demonstrate the issue where gRPC Context is incorrectly propagated in async threads.

## Issue Description

When using OpenTelemetry's gRPC instrumentation, `ContextStorageBridge` stores the gRPC Context in the OpenTelemetry Context (via the "grpc-context" key). If `Context.current().wrap(runnable)` is used to propagate the OpenTelemetry Context to async threads, the gRPC Context will also be propagated together, which may cause issues.

### Issue Scenario

1. **Upstream gRPC Service (UpstreamService)**: Listens on port 9091, provides basic services
2. **Downstream gRPC Service (DownstreamService)**: Listens on port 9090, receives external requests
3. **Issue Flow**:
   - Downstream service receives gRPC request (creates gRPC Context)
   - Downstream service internally synchronously calls upstream service (normal)
   - Downstream service creates async task, `ThreadTransferOtelDecorator` uses `Context.current().wrap(runnable)` to propagate OpenTelemetry Context
   - Since OpenTelemetry Context contains gRPC Context, the gRPC Context is also propagated to the async thread
   - When calling the upstream service again in the async task, an error occurs due to incorrect gRPC Context state

## Project Structure

```
grpc-otel-context-bug-reproduction/
├── pom.xml                                    # Maven configuration
├── README.md                                  # This document
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/reproduction/
│   │   │       ├── Application.java           # Spring Boot main class
│   │   │       ├── config/
│   │   │       │   ├── ThreadPoolConfig.java  # Thread pool configuration
│   │   │       │   ├── ThreadTransferOtelDecorator.java  # Where the issue is
│   │   │       │   ├── EventBusConfig.java    # Async event bus configuration
│   │   │       │   ├── GrpcServerConfig.java  # gRPC server configuration
│   │   │       │   └── OpenTelemetryConfig.java  # OpenTelemetry configuration
│   │   │       ├── upstream/                  # Upstream service
│   │   │       │   ├── grpc/                  # gRPC service implementation
│   │   │       │   └── service/               # Business service
│   │   │       ├── downstream/                # Downstream service
│   │   │       │   ├── grpc/                  # gRPC service implementation
│   │   │       │   └── service/               # Business service
│   │   │       ├── client/
│   │   │       │   └── UpstreamClient.java    # Upstream service client
│   │   │       └── event/
│   │   │           ├── ProcessEvent.java       # Event class
│   │   │           └── ProcessEventHandler.java # Event handler (executed asynchronously)
│   │   └── resources/
│   │       ├── application.yml                # Spring Boot configuration
│   │       └── proto/
│   │           ├── upstream_service.proto     # Upstream service proto definition
│   │           └── downstream_service.proto   # Downstream service proto definition
│   └── test/
└── scripts/
    └── test.sh                                # Test script
```

## Build and Run

### Prerequisites

- Java 21+
- Maven 3.6+
- grpcurl (for testing)

### Build the Project

```bash
cd grpc-otel-context-bug-reproduction
mvn clean compile
```

### Run the Application

```bash
mvn spring-boot:run
```

After the application starts:
- Downstream gRPC service runs on port **9090**
- Upstream gRPC service runs on port **9091**

### Run Tests

Execute in another terminal from the root directory:

```bash
chmod +x scripts/test.sh
./scripts/test.sh
```

Or use grpcurl directly from the root directory:

```bash
grpcurl -plaintext \
  -import-path ./src/main/resources/proto \
  -proto downstream_service.proto \
  -d '{"id":"test-123","data":"test data"}' \
  127.0.0.1:9090 \
  com.example.reproduction.downstream.grpc.DownstreamService/Handle
```

## Verify the Issue

Check the application logs, you should see:

1. **Downstream service receives request**:
   ```
   Downstream gRPC service received request, id: test-123
   DownstreamService.handle called with id: test-123
   Current gRPC Context in downstream service: ...
   ```

2. **First call to upstream service (synchronous, normal)**:
   ```
   Making first call to upstream service...
   UpstreamClient.process called with id: test-123
   First upstream call succeeded: true
   ```

3. **Event sent to async thread**:
   ```
   Posting event to async event bus, current gRPC Context: ...
   ```

4. **Context detection in async thread (where the issue is)**:
   ```
   === ProcessEventHandler.handle called in async thread ===
   Thread: AsyncTask-1
   gRPC Context in async thread: ... (NOT ROOT - this is the bug!)
   Is gRPC Context ROOT? false
   ```

5. **Second call to upstream service (async, may fail)**:
   ```
   Making second call to upstream service from async thread...
   Current gRPC Context before second call: ...
   ERROR: Second upstream call failed!
   ```

## Root Cause

The issue is in the `ThreadTransferOtelDecorator.decorate()` method:

```java
public Runnable decorate(Runnable runnable) {
    Context otelContext = Context.current();
    // BUG: This wraps with OpenTelemetry context,
    // but OpenTelemetry context contains gRPC context,
    // so gRPC context is also propagated incorrectly
    return otelContext.wrap(runnable);
}
```

When OpenTelemetry's `ContextStorageBridge` is enabled, the gRPC Context is stored in the OpenTelemetry Context. When using `Context.current().wrap()`, the entire OpenTelemetry Context (including the gRPC Context within it) is propagated to the new thread, causing the gRPC Context to be incorrectly activated in async threads.

## Solution Approach

The solution should be in `ThreadTransferOtelDecorator`, removing the gRPC Context from the OpenTelemetry Context before wrapping the runnable:

1. Get the current OpenTelemetry Context
2. Check if it contains gRPC Context (via the "grpc-context" key)
3. If it contains it, create a new Context without the gRPC Context
4. Use the new Context to wrap the runnable

## 依赖版本

- Spring Boot: 3.2.0
- gRPC: 1.60.1
- OpenTelemetry: 1.32.0
- OpenTelemetry Instrumentation (gRPC 1.6): 2.1.0-alpha

