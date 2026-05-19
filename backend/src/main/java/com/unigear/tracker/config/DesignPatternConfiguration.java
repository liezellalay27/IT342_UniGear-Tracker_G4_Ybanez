package com.unigear.tracker.config;

import com.unigear.tracker.pattern.observer.EventPublisher;
import com.unigear.tracker.pattern.observer.observers.NotificationObserver;
import com.unigear.tracker.pattern.observer.observers.AuditLoggingObserver;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Configuration for Design Pattern implementations
 * Sets up singleton instances and wires observer dependencies
 */
@Configuration
public class DesignPatternConfiguration {
    
    /**
     * Create singleton EventPublisher bean
     * This is the central event bus for all system events
     */
    @Bean
    public EventPublisher eventPublisher() {
        return new EventPublisher();
    }
    
    /**
     * Register NotificationObserver to EventPublisher
     * When system events are published, NotificationObserver reacts
     * (e.g., sends email when request is approved)
     */
    @Bean
    public NotificationObserver notificationObserver() {
        return new NotificationObserver();
    }
    
    /**
     * Register AuditLoggingObserver to EventPublisher
     * All system events are logged for compliance and troubleshooting
     */
    @Bean
    public AuditLoggingObserver auditLoggingObserver() {
        return new AuditLoggingObserver();
    }
    
    /**
     * Observer Registration Component
     * Wires observers to the event publisher after all beans are created
     */
    @Component
    public static class ObserverRegistrar {
        private final EventPublisher eventPublisher;
        private final NotificationObserver notificationObserver;
        private final AuditLoggingObserver auditLoggingObserver;
        
        public ObserverRegistrar(
                EventPublisher eventPublisher,
                NotificationObserver notificationObserver,
                AuditLoggingObserver auditLoggingObserver) {
            this.eventPublisher = eventPublisher;
            this.notificationObserver = notificationObserver;
            this.auditLoggingObserver = auditLoggingObserver;
        }
        
        /**
         * Register all observers after component initialization
         */
        @PostConstruct
        public void registerObservers() {
            eventPublisher.subscribe(notificationObserver);
            eventPublisher.subscribe(auditLoggingObserver);
            
            // Future observers can be registered here
            // eventPublisher.subscribe(analyticsObserver);
            // eventPublisher.subscribe(websocketObserver);
        }
    }
}
