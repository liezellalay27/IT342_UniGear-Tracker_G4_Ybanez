package com.unigear.tracker.pattern.observer;

import com.unigear.tracker.pattern.singleton.LoggerService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer Pattern: Subject (Observable)
 * 
 * Manages the list of observers and notifies them when systemevents occur.
 * Thread-safe implementation using CopyOnWriteArrayList for concurrent access.
 * 
 * Usage:
 * // In your service class
 * @Autowired
 * private EventPublisher eventPublisher;
 * 
 * // When something happens
 * eventPublisher.publish(new SystemEvent(
 *     EventType.REQUEST_APPROVED,
 *     "RequestService",
 *     requestId,
 *     "REQUEST",
 *     userEmail,
 *     "Request has been approved"
 * ));
 */
@Component
public class EventPublisher {
    
    private final List<EventObserver> observers = new CopyOnWriteArrayList<>();
    private static final LoggerService logger = LoggerService.getInstance();
    
    /**
     * Register an observer to receive events
     */
    public void subscribe(EventObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            logger.logDebug("EventPublisher", 
                "Observer subscribed: " + observer.getObserverName());
        }
    }
    
    /**
     * Unregister an observer
     */
    public void unsubscribe(EventObserver observer) {
        if (observers.remove(observer)) {
            logger.logDebug("EventPublisher", 
                "Observer unsubscribed: " + observer.getObserverName());
        }
    }
    
    /**
     * Publish an event to all subscribed observers
     */
    public void publish(SystemEvent event) {
        logger.logDebug("EventPublisher", 
            String.format("Publishing event: %s", event.getEventType()));
        
        for (EventObserver observer : observers) {
            // Only notify observers that support this event type
            if (observer.supportsEventType(event.getEventType().name())) {
                try {
                    observer.onEvent(event);
                } catch (Exception e) {
                    logger.logError("EventPublisher", 
                        "Error notifying observer " + observer.getObserverName(), e);
                }
            }
        }
    }
    
    /**
     * Get count of registered observers (for monitoring)
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Get count of observers that support a specific event type
     */
    public int getObserverCountForEventType(String eventType) {
        return (int) observers.stream()
            .filter(o -> o.supportsEventType(eventType))
            .count();
    }
    
    /**
     * Get all registered observers (for monitoring/debugging)
     */
    public List<EventObserver> getObservers() {
        return new ArrayList<>(observers);
    }
    
    /**
     * Clear all observers (mainly for testing)
     */
    public void clearObservers() {
        observers.clear();
    }
}
