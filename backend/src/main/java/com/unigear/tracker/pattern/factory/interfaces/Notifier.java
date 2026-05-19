package com.unigear.tracker.pattern.factory.interfaces;

/**
 * Factory Method Pattern: Notification Strategy Interface
 * 
 * Defines the contract for different notification implementations.
 * Future implementations can include: Email, SMS, Push, Slack, etc.
 */
public interface Notifier {
    /**
     * Send notification to recipient
     */
    void send(String recipient, String subject, String message);
    
    /**
     * Get notifier type
     */
    String getNotifierType();
    
    /**
     * Check if notifier is enabled
     */
    boolean isEnabled();
}
