package com.unigear.tracker.pattern.factory.notifiers;

import com.unigear.tracker.pattern.factory.interfaces.Notifier;
import com.unigear.tracker.pattern.singleton.LoggerService;

/**
 * Factory Method Pattern: Concrete Notifier Implementation
 * 
 * Email notification implementation.
 * Currently logs notifications; can be extended to send actual emails via JavaMailSender.
 */
public class EmailNotifier implements Notifier {
    
    private final boolean enabled;
    private static final LoggerService logger = LoggerService.getInstance();
    
    public EmailNotifier(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void send(String recipient, String subject, String message) {
        if (!enabled) {
            logger.logWarning("EmailNotifier", "Email notification is disabled");
            return;
        }
        
        try {
            // TODO: Implement actual email sending using Spring's JavaMailSender
            // This is a placeholder implementation
            logger.logDebug("EmailNotifier", 
                String.format("Email sent to %s with subject: %s", recipient, subject));
            
            // Future implementation:
            // SimpleMailMessage mailMessage = new SimpleMailMessage();
            // mailMessage.setTo(recipient);
            // mailMessage.setSubject(subject);
            // mailMessage.setText(message);
            // mailSender.send(mailMessage);
            
        } catch (Exception e) {
            logger.logError("EmailNotifier", "Failed to send email to " + recipient, e);
        }
    }
    
    @Override
    public String getNotifierType() {
        return "EMAIL";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
