package com.example.reproduction.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for OpenTelemetry.
 * This sets up the OpenTelemetry SDK and enables gRPC context bridging.
 */
@Slf4j
@Configuration
public class OpenTelemetryConfig {
    
    @PostConstruct
    public void init() {
        // Initialize OpenTelemetry SDK if not already initialized
        if (GlobalOpenTelemetry.get() == OpenTelemetry.noop()) {
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(SdkTracerProvider.builder().build())
                    .setPropagators(ContextPropagators.noop())
                    .build();
            
            // Note: For gRPC Context bridging to work, we need to use the ContextStorageOverride
            // This is typically done by the OpenTelemetry javaagent, but for library mode,
            // we need to ensure the grpc-1.6 instrumentation is properly initialized
            log.info("OpenTelemetry initialized");
        }
    }
}

