package com.unigear.tracker.pattern.observer.observers;

import com.unigear.tracker.pattern.observer.EventObserver;
import com.unigear.tracker.pattern.observer.SystemEvent;
import com.unigear.tracker.pattern.factory.NotificationFactory;
import com.unigear.tracker.pattern.factory.interfaces.Notifier;
import com.unigear.tracker.pattern.singleton.LoggerService;

/**
 * Observer Pattern: Concrete Observer Implementation
 * 
 * Sends notifications when events occur.
 * Reacts to:
 * - REQUEST_APPROVED: Send notification to student
 * - REQUEST_REJECTED: Send notification to student
 * - EQUIPMENT_UPDATED: Notify interested users
 */
public class NotificationObserver implements EventObserver {
    
    private final Notifier emailNotifier;
    private static final LoggerService logger = LoggerService.getInstance();
    
    public NotificationObserver() {
        Notifier notifier = null;
        try {
            notifier = NotificationFactory.createNotifier(NotificationFactory.NotificationType.EMAIL);
        } catch (Exception e) {
            logger.logWarning("NotificationObserver", "Failed to initialize email notifier: " + e.getMessage());
            logger.logWarning("NotificationObserver", "Continuing with notifications disabled. Configure JWT/email settings to enable.");
            // Continue without email notifier - it's optional during development
        }
        this.emailNotifier = notifier;
    }
    
    @Override
    public void onEvent(SystemEvent event) {
        if (emailNotifier == null) {
            logger.logWarning("NotificationObserver", "Email notifier not available, skipping notification for event: " + event.getEventType());
            return;
        }
        
        switch (event.getEventType()) {
            case REQUEST_APPROVED:
                handleRequestApproved(event);
                break;
            case REQUEST_REJECTED:
                handleRequestRejected(event);
                break;
            case EQUIPMENT_UPDATED:
                handleEquipmentUpdated(event);
                break;
            case REQUEST_RETURNED:
                handleRequestReturned(event);
                break;
            default:
                // Event type not handled by this observer
        }
    }
    
    private void handleRequestApproved(SystemEvent event) {
        String studentEmail = event.getData() instanceof String ? (String) event.getData() : event.getActor();
        emailNotifier.send(
            studentEmail,
            "Request Approved",
            "Your equipment request has been approved. Please pick up your equipment."
        );
        logger.logDebug("NotificationObserver", 
            "Approval notification sent to: " + studentEmail);
    }
    
    private void handleRequestRejected(SystemEvent event) {
        String studentEmail = event.getData() instanceof String ? (String) event.getData() : event.getActor();
        emailNotifier.send(
            studentEmail,
            "Request Rejected",
            "Your equipment request has been rejected. Please contact support for more information."
        );
        logger.logDebug("NotificationObserver", 
            "Rejection notification sent to: " + studentEmail);
    }
    
    private void handleEquipmentUpdated(SystemEvent event) {
        // Could notify administrators or interested users
        logger.logDebug("NotificationObserver", 
            "Equipment update notification triggered for equipment: " + event.getTargetId());
    }
    
    private void handleRequestReturned(SystemEvent event) {
        // Could send confirmation email
        logger.logDebug("NotificationObserver", 
            "Return confirmation notification triggered for request: " + event.getTargetId());
    }
    
    @Override
    public String getObserverName() {
        return "NotificationObserver";
    }
    
    @Override
    public boolean supportsEventType(String eventType) {
        return eventType.equals("REQUEST_APPROVED") ||
               eventType.equals("REQUEST_REJECTED") ||
               eventType.equals("EQUIPMENT_UPDATED") ||
               eventType.equals("REQUEST_RETURNED");
    }
}
