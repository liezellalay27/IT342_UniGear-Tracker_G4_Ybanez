package com.unigear.tracker.features.auth.service;

import com.unigear.tracker.features.auth.config.AuthEmailProperties;
import com.unigear.tracker.features.auth.dto.AuthResponse;
import com.unigear.tracker.features.auth.dto.ForgotPasswordRequest;
import com.unigear.tracker.features.auth.dto.LoginRequest;
import com.unigear.tracker.features.auth.dto.RegisterRequest;
import com.unigear.tracker.features.auth.dto.ResetPasswordRequest;
import com.unigear.tracker.features.auth.entity.PasswordResetToken;
import com.unigear.tracker.features.auth.repository.PasswordResetTokenRepository;
import com.unigear.tracker.features.auth.service.EmailService;
import com.unigear.tracker.features.user.entity.User;
import com.unigear.tracker.features.user.repository.UserRepository;
import com.unigear.tracker.features.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthEmailProperties emailProperties;

    @Value("${app.admin.email:admin@unigear.com}")
    private String adminEmail;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
            request.getName(),
            request.getEmail(),
            hashedPassword
        );
        user.setRole(resolveRoleForEmail(request.getEmail()));

        User savedUser = userRepository.save(user);

        AuthResponse response = new AuthResponse(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getRole().name()
        );
        response.setMessage("Registration successful");

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
        } catch (RuntimeException ex) {
            log.warn("Welcome email could not be sent to {}: {}", savedUser.getEmail(), ex.getMessage());
        }
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateJwtToken(user.getEmail());

        AuthResponse response = new AuthResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        );
        response.setAccessToken(token);
        return response;
    }

    @Transactional
    public AuthResponse requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with that email"));

        passwordResetTokenRepository.deleteByUserEmail(user.getEmail());

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(emailProperties.passwordResetTokenExpiryMinutes()));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetToken.getToken());

        return new AuthResponse("Password reset instructions have been sent to your email.");
    }

    public AuthResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new AuthResponse("Password reset successful. You can now log in with your new password.");
    }

    /**
     * Authenticate or register a user using Google OAuth2 principal
     */
    public AuthResponse authenticateWithGoogleOAuth2User(OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            throw new IllegalArgumentException("OAuth2User is null");
        }

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("OAuth2 provider did not return an email");
        }

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User(name != null ? name : "Google User", email, null);
            newUser.setPicture(picture);
            newUser.setRole(resolveRoleForEmail(email));
            return userRepository.save(newUser);
        });

        // Generate token
        String token = jwtUtil.generateJwtToken(user.getEmail());

        AuthResponse response = new AuthResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        );
        response.setAccessToken(token);
        return response;
    }

    private User.Role resolveRoleForEmail(String email) {
        if (email != null && email.equalsIgnoreCase(adminEmail)) {
            return User.Role.ADMIN;
        }
        return User.Role.STUDENT;
    }
}
