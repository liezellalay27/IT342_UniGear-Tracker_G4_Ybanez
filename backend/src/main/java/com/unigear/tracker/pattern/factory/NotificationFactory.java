package com.unigear.tracker.pattern.factory;

import com.unigear.tracker.pattern.factory.interfaces.Notifier;
import com.unigear.tracker.pattern.factory.notifiers.EmailNotifier;
import com.unigear.tracker.pattern.singleton.ConfigurationManager;

/**
 * Factory Method Pattern Implementation: NotificationFactory
 * 
 * Creates appropriate notifier instances based on type.
 * This enables easy addition of new notification types (SMS, Push, Slack) without
 * modifying existing code.
 * 
 * Usage:
 * Notifier emailNotifier = NotificationFactory.createNotifier(NotificationType.EMAIL);
 * emailNotifier.send("user@example.com", "Approval", "Your request has been approved");
 */
public class NotificationFactory {
    
    public enum NotificationType {
        EMAIL,
        SMS,
        PUSH,
        SLACK
    }
    
    /**
     * Create notifier based on type
     * @param type the type of notifier to create
     * @return appropriate Notifier implementation
     * @throws IllegalArgumentException if type is not supported
     */
    public static Notifier createNotifier(NotificationType type) {
        ConfigurationManager config = ConfigurationManager.getInstance();
        
        switch (type) {
            case EMAIL:
                return new EmailNotifier(config.isEmailNotificationEnabled());
            
            case SMS:
                // Future implementation
                throw new UnsupportedOperationException("SMS notifier not yet implemented");
            
            case PUSH:
                // Future implementation
                throw new UnsupportedOperationException("Push notifier not yet implemented");
            
            case SLACK:
                // Future implementation
                throw new UnsupportedOperationException("Slack notifier not yet implemented");
            
            default:
                throw new IllegalArgumentException("Unknown notifier type: " + type);
        }
    }
    
    /**
     * Create notifier by string name
     * @param typeName the name of the notifier type
     * @return appropriate Notifier implementation
     */
    public static Notifier createNotifier(String typeName) {
        try {
            return createNotifier(NotificationType.valueOf(typeName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown notifier type: " + typeName);
        }
    }
    
    /**
     * Check if a notification type is enabled
     */
    public static boolean isNotifierEnabled(NotificationType type) {
        try {
            return createNotifier(type).isEnabled();
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }
}
