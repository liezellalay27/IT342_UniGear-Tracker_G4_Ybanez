package com.unigear.tracker.features.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record AuthEmailProperties(
        String frontendBaseUrl,
        long passwordResetTokenExpiryMinutes,
        String fromAddress,
        String fromName
) {
    public String loginUrl() {
        return baseUrl() + "/login";
    }

    public String passwordResetUrl(String token) {
        return baseUrl() + "/forgot-password?token=" + token;
    }

    public String senderAddress() {
        return fromAddress == null ? "" : fromAddress.trim();
    }

    public String senderName() {
        return (fromName == null || fromName.isBlank()) ? "UniGear Tracker" : fromName.trim();
    }

    public String baseUrl() {
        return (frontendBaseUrl == null || frontendBaseUrl.isBlank())
                ? "http://localhost:3000"
                : frontendBaseUrl.trim();
    }
}