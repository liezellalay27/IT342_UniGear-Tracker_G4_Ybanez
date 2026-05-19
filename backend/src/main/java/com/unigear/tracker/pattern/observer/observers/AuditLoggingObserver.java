package com.unigear.tracker.pattern.observer.observers;

import com.unigear.tracker.pattern.observer.EventObserver;
import com.unigear.tracker.pattern.observer.SystemEvent;
import com.unigear.tracker.pattern.singleton.LoggerService;

/**
 * Observer Pattern: Concrete Observer Implementation
 * 
 * Logs all events for audit trails and debugging.
 * Tracks all system activity for compliance and monitoring.
 * 
 * Reacts to all events:
 * - Maintains audit log
 * - Enables system monitoring
 * - Supports compliance requirements
 */
public class AuditLoggingObserver implements EventObserver {
    
    private static final LoggerService logger = LoggerService.getInstance();
    
    @Override
    public void onEvent(SystemEvent event) {
        logEvent(event);
    }
    
    private void logEvent(SystemEvent event) {
        String logMessage = String.format(
            "[AUDIT] Event: %s | Target: %s:%d | Actor: %s | Description: %s",
            event.getEventType(),
            event.getTargetType(),
            event.getTargetId(),
            event.getActor(),
            event.getDescription()
        );
        
        logger.logWarning("AuditLog", logMessage);
    }
    
    @Override
    public String getObserverName() {
        return "AuditLoggingObserver";
    }
    
    @Override
    public boolean supportsEventType(String eventType) {
        // This observer supports ALL event types
        return true;
    }
}
