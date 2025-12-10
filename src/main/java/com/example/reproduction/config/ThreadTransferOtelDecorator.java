package com.example.reproduction.config;

import io.opentelemetry.context.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;

/**
 * TaskDecorator that transfers OpenTelemetry context to async threads.
 * This is where the bug occurs: it also transfers gRPC context incorrectly.
 */
@Slf4j
public class ThreadTransferOtelDecorator implements TaskDecorator {
    
    @Override
    public Runnable decorate(Runnable runnable) {
        // Get current OpenTelemetry context
        Context otelContext = Context.current();
        log.debug("ThreadTransferOtelDecorator: capturing OpenTelemetry context for async task");
        
        // BUG: This wraps the runnable with OpenTelemetry context,
        // but OpenTelemetry context contains gRPC context (via ContextStorageBridge),
        // so gRPC context is also propagated incorrectly to the async thread
        return otelContext.wrap(runnable);
    }
}

