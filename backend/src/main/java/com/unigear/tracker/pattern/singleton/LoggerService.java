package com.unigear.tracker.pattern.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Singleton Pattern Implementation: LoggerService
 * 
 * Ensures only one instance of the logging service exists throughout the application.
 * This provides centralized, consistent logging across all components.
 * 
 * Benefits:
 * - Centralized log configuration
 * - Consistent log formatting
 * - Single point for log level management
 * - Audit trail for all system events
 */
@Component
public class LoggerService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggerService.class);
    private static LoggerService instance;
    
    // Private constructor to prevent external instantiation
    private LoggerService() {}
    
    /**
     * Get the singleton instance of LoggerService
     * Note: Spring will manage the singleton lifecycle through @Component
     */
    public static synchronized LoggerService getInstance() {
        if (instance == null) {
            instance = new LoggerService();
        }
        return instance;
    }
    
    // ============ Log Methods ============
    
    /**
     * Log authentication events
     */
    public void logAuthEvent(String email, String eventType, String details) {
        logger.info("[AUTH] Email: {}, Event: {}, Details: {}", email, eventType, details);
    }
    
    /**
     * Log equipment-related events
     */
    public void logEquipmentEvent(Long equipmentId, String eventType, String details) {
        logger.info("[EQUIPMENT] Equipment ID: {}, Event: {}, Details: {}", equipmentId, eventType, details);
    }
    
    /**
     * Log request-related events
     */
    public void logRequestEvent(Long requestId, String eventType, String details) {
        logger.info("[REQUEST] Request ID: {}, Event: {}, Details: {}", requestId, eventType, details);
    }
    
    /**
     * Log admin actions
     */
    public void logAdminAction(String adminEmail, String action, String targetId, String details) {
        logger.warn("[ADMIN] Admin: {}, Action: {}, Target ID: {}, Details: {}", 
            adminEmail, action, targetId, details);
    }
    
    /**
     * Log errors
     */
    public void logError(String component, String message, Exception exception) {
        logger.error("[ERROR] Component: {}, Message: {}", component, message, exception);
    }
    
    /**
     * Log warnings
     */
    public void logWarning(String component, String message) {
        logger.warn("[WARNING] Component: {}, Message: {}", component, message);
    }
    
    /**
     * Log debug information
     */
    public void logDebug(String component, String message) {
        logger.debug("[DEBUG] Component: {}, Message: {}", component, message);
    }
    
    /**
     * Log security-related events
     */
    public void logSecurityEvent(String eventType, String details) {
        logger.warn("[SECURITY] Event: {}, Details: {}", eventType, details);
    }
    
    /**
     * Log API access
     */
    public void logApiAccess(String endpoint, String method, String userEmail, int statusCode) {
        logger.info("[API] Endpoint: {}, Method: {}, User: {}, Status: {}", 
            endpoint, method, userEmail, statusCode);
    }
}
