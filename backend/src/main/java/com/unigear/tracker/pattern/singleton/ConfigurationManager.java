package com.unigear.tracker.pattern.singleton;

/**
 * Singleton Pattern Implementation: ConfigurationManager
 * 
 * Centralizes all application configuration settings in a single instance.
 * Initialized by Spring component ConfigurationInitializer at startup.
 * 
 * This is a pure Singleton (not a Spring component) - accessed via getInstance()
 * 
 * Benefits:
 * - Single source of truth for configuration
 * - Easy to test with different configurations
 * - Can add validation for critical settings
 * - Provides default values for optional settings
 */
public class ConfigurationManager {
    
    private static ConfigurationManager instance;
    
    // ============ JWT Configuration ============
    private String jwtSecret = "mySecretKeyForJWTTokenGenerationAndValidation12345678";
    private long jwtExpiration = 86400000;  // 24 hours
    
    // ============ Admin Configuration ============
    private String adminEmail = "admin@unigear.com";
    
    // ============ File Upload Configuration ============
    private long maxFileSize = 10485760;  // 10MB
    private String allowedFileTypes = "application/pdf";
    
    // ============ Database Configuration ============
    private String databaseUrl = "jdbc:postgresql://localhost:5432/unigear";
    
    // ============ Authentication Configuration ============
    private String googleClientId = "";
    private String googleClientSecret = "";
    
    // ============ API Configuration ============
    private String apiBaseUrl = "http://localhost:8080";
    private String frontendUrl = "http://localhost:3000";
    
    // ============ Notification Configuration ============
    private boolean notificationEnabled = true;
    private boolean emailNotificationEnabled = true;
    
    // Private constructor
    private ConfigurationManager() {
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    /**
     * Initialize singleton with configuration values (called by Spring at startup)
     */
    public static synchronized void initialize(
            String jwtSecretValue,
            Long jwtExpirationValue,
            String adminEmailValue) {
        
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        
        if (jwtSecretValue != null && !jwtSecretValue.isBlank()) {
            instance.jwtSecret = jwtSecretValue;
        }
        
        if (jwtExpirationValue != null && jwtExpirationValue > 0) {
            instance.jwtExpiration = jwtExpirationValue;
        }
        
        if (adminEmailValue != null && !adminEmailValue.isBlank()) {
            instance.adminEmail = adminEmailValue;
        }
    }
    
    // ============ Getters ============
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public long getJwtExpiration() {
        return jwtExpiration;
    }
    
    public String getAdminEmail() {
        return adminEmail;
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
    
    public String getAllowedFileTypes() {
        return allowedFileTypes;
    }
    
    public String getDatabaseUrl() {
        return databaseUrl;
    }
    
    public String getGoogleClientId() {
        return googleClientId;
    }
    
    public String getGoogleClientSecret() {
        return googleClientSecret;
    }
    
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    public String getFrontendUrl() {
        return frontendUrl;
    }
    
    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }
    
    public boolean isEmailNotificationEnabled() {
        return emailNotificationEnabled;
    }
    
    /**
     * Check if file size is within allowed limit
     */
    public boolean isFileSizeValid(long fileSize) {
        return fileSize <= maxFileSize;
    }
    
    /**
     * Check if file type is allowed
     */
    public boolean isFileTypeAllowed(String contentType) {
        return allowedFileTypes.contains(contentType);
    }
}
