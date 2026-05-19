package com.unigear.tracker.service;

import com.unigear.tracker.entity.User;
import com.unigear.tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.admin.bootstrap.enabled:true}")
    private boolean adminBootstrapEnabled;

    @Value("${app.admin.email:admin@unigear.com}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminAccountInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (!adminBootstrapEnabled) {
            return;
        }

        if (adminEmail == null || adminEmail.isBlank()) {
            logger.warn("Admin bootstrap skipped: app.admin.email is empty.");
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            logger.warn("Admin bootstrap skipped: app.admin.password is empty.");
            return;
        }

        Optional<User> existing = userRepository.findByEmail(adminEmail);
        if (existing.isPresent()) {
            User user = existing.get();
            boolean changed = false;

            if (user.getRole() != User.Role.ADMIN) {
                user.setRole(User.Role.ADMIN);
                changed = true;
            }

            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(adminPassword));
                changed = true;
            }

            if (changed) {
                userRepository.save(user);
            }
            logger.info("Admin account ready for email: {}", adminEmail);
            return;
        }

        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);

        logger.info("Created dedicated admin account for email: {}", adminEmail);
    }
}
