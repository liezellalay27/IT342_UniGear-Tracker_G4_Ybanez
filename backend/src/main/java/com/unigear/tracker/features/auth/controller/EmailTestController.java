package com.unigear.tracker.features.auth.controller;

import com.unigear.tracker.features.auth.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/public/debug/send-test-email")
    public ResponseEntity<?> sendTest(@RequestParam String to) {
        // send a created template sample
        java.util.Map<String,Object> model = new java.util.HashMap<>();
        model.put("requestId", 12345L);
        model.put("equipmentName", "Camera");
        model.put("quantity", 1);
        String html = emailService.renderTemplate("request-created", model);
        if (html != null) {
            emailService.sendHtmlEmail(to, "Test: Request Received", html);
            return ResponseEntity.ok("Email sent (template)");
        } else {
            emailService.sendGenericEmail(to, "Test Email", "This is a test email from UniGear Tracker.");
            return ResponseEntity.ok("Email sent (plain)");
        }
    }
}
