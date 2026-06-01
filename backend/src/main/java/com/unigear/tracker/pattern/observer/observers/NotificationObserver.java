package com.unigear.tracker.pattern.observer.observers;

import com.unigear.tracker.pattern.observer.EventObserver;
import com.unigear.tracker.pattern.observer.SystemEvent;
import com.unigear.tracker.features.auth.service.EmailService;
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
    
    private final EmailService emailService;
    private static final LoggerService logger = LoggerService.getInstance();
    
    public NotificationObserver(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void onEvent(SystemEvent event) {
        if (emailService == null) {
            logger.logWarning("NotificationObserver", "Email service not available, skipping notification for event: " + event.getEventType());
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
        String subject = "Your UniGear equipment request is approved";
        // render template
        try {
                java.util.Map<String,Object> model = new java.util.HashMap<>();
                model.put("requestId", event.getTargetId());
                model.put("equipmentName", event.getMetadata().getOrDefault("equipmentName", ""));
                model.put("quantity", event.getMetadata().getOrDefault("quantity", ""));
                model.put("studentName", event.getMetadata().getOrDefault("studentName", ""));
                model.put("schoolIdNumber", event.getMetadata().getOrDefault("schoolIdNumber", ""));
                model.put("borrowDate", event.getMetadata().getOrDefault("borrowDate", ""));
                model.put("returnDate", event.getMetadata().getOrDefault("returnDate", ""));
                model.put("yearLevel", event.getMetadata().getOrDefault("yearLevel", ""));
                model.put("course", event.getMetadata().getOrDefault("course", ""));
                model.put("notes", event.getMetadata().getOrDefault("notes", ""));
            String rendered = emailService.renderTemplate("request-approved", model);
            if (rendered != null) {
                byte[] approvalPdf = emailService.generateApprovalPdf(model);
                String filename = "request-" + event.getTargetId() + "-approval.pdf";
                emailService.sendHtmlEmailWithAttachment(studentEmail, subject, rendered, approvalPdf, filename, "application/pdf");
            } else {
                // fallback
                String body = "Hi,\n\nYour equipment request has been approved. Request ID: " + event.getTargetId();
                emailService.sendGenericEmail(studentEmail, subject, body);
            }
        } catch (Exception e) {
            logger.logWarning("NotificationObserver", "Failed to render approval template: " + e.getMessage());
        }
        logger.logDebug("NotificationObserver", 
            "Approval notification sent to: " + studentEmail);
    }
    
    private void handleRequestRejected(SystemEvent event) {
        String studentEmail = event.getData() instanceof String ? (String) event.getData() : event.getActor();
        String subject = "UniGear request update: rejected";
        String reason = event.getMetadata().getOrDefault("rejectionReason", "No reason provided").toString();
        try {
            java.util.Map<String,Object> model = new java.util.HashMap<>();
            model.put("reason", reason);
            String rendered = emailService.renderTemplate("request-rejected", model);
            if (rendered != null) {
                emailService.sendHtmlEmail(studentEmail, subject, rendered);
            } else {
                emailService.sendGenericEmail(studentEmail, subject, "Your request was rejected. Reason: " + reason);
            }
        } catch (Exception e) {
            logger.logWarning("NotificationObserver", "Failed to render rejection template: " + e.getMessage());
        }
        logger.logDebug("NotificationObserver", 
            "Rejection notification sent to: " + studentEmail);
    }

    private void handleRequestCreated(SystemEvent event) {
        String studentEmail = event.getData() instanceof String ? (String) event.getData() : event.getActor();
        String subject = "UniGear request received";
        try {
                java.util.Map<String,Object> model = new java.util.HashMap<>();
                model.put("requestId", event.getTargetId());
                model.put("equipmentName", event.getMetadata().getOrDefault("equipmentName", ""));
                model.put("quantity", event.getMetadata().getOrDefault("quantity", ""));
            String rendered = emailService.renderTemplate("request-created", model);
            if (rendered != null) {
                emailService.sendHtmlEmail(studentEmail, subject, rendered);
            } else {
                emailService.sendGenericEmail(studentEmail, subject, "We received your request: " + event.getTargetId());
            }
        } catch (Exception e) {
            logger.logWarning("NotificationObserver", "Failed to render creation template: " + e.getMessage());
        }
        logger.logDebug("NotificationObserver", "Creation notification sent to: " + studentEmail);
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
               eventType.equals("REQUEST_RETURNED") ||
               eventType.equals("REQUEST_CREATED");
    }
}
