package com.example.reproduction;

import com.example.reproduction.event.ProcessEventHandler;
import com.google.common.eventbus.AsyncEventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {
    
    @Autowired
    private ProcessEventHandler processEventHandler;
    
    @Autowired
    private AsyncEventBus asyncEventBus;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    public void run(String... args) {
        // Register event handler with AsyncEventBus
        asyncEventBus.register(processEventHandler);
        log.info("ProcessEventHandler registered with AsyncEventBus");
        log.info("Application started. Downstream service on port 9090, Upstream service on port 9091");
    }
}

