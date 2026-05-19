package com.unigear.tracker.config;

import com.unigear.tracker.pattern.singleton.ConfigurationManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration Initializer Component
 * 
 * Bridges Spring's @Value injection with the ConfigurationManager Singleton.
 * Ensures configuration is loaded from application.properties at startup.
 */
@Component
public class ConfigurationInitializer {
    
    @Value("${app.jwt.secret:mySecretKeyForJWTTokenGenerationAndValidation12345678}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")
    private Long jwtExpiration;
    
    @Value("${app.admin.email:admin@unigear.com}")
    private String adminEmail;
    
    /**
     * Initialize ConfigurationManager singleton with Spring-injected values
     * Called automatically after bean construction
     */
    @PostConstruct
    public void initialize() {
        ConfigurationManager.initialize(jwtSecret, jwtExpiration, adminEmail);
    }
}
