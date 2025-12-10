package com.example.reproduction.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EventBusConfig {
    
    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }
    
    @Bean
    public AsyncEventBus asyncEventBus(ThreadPoolTaskExecutor coreThreadPoolTaskExecutor) {
        // Use the thread pool with ThreadTransferOtelDecorator
        // This causes gRPC context to be incorrectly propagated to async event handlers
        return new AsyncEventBus(coreThreadPoolTaskExecutor, new com.google.common.eventbus.SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, com.google.common.eventbus.SubscriberExceptionContext context) {
//                System.err.println("EventBus exception: " + exception.getMessage());
//                exception.printStackTrace();
            }
        });
    }
}

