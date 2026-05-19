package com.unigear.tracker.pattern.observer;

/**
 * Observer Pattern: Base Observer Interface
 * 
 * Defines the contract for all objects that want to be notified of events.
 * Implementations can be:
 * - RequestStatusObserver: React to request status changes
 * - EquipmentAvailabilityObserver: React to equipment availability changes
 * - NotificationObserver: Send notifications on events
 * - AnalyticsObserver: Track events for analytics
 */
public interface EventObserver {
    /**
     * Called when an event occurs
     * @param event the event that occurred
     */
    void onEvent(SystemEvent event);
    
    /**
     * Get observer name for logging and debugging
     */
    String getObserverName();
    
    /**
     * Check if observer supports this event type
     */
    boolean supportsEventType(String eventType);
}
