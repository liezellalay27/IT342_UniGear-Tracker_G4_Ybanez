package com.unigear.tracker.features.auth.service;

import com.unigear.tracker.dto.AuthResponse;
import com.unigear.tracker.dto.LoginRequest;
import com.unigear.tracker.dto.RegisterRequest;
import com.unigear.tracker.entity.User;
import com.unigear.tracker.repository.UserRepository;
import com.unigear.tracker.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.admin.email:admin@unigear.com}")
    private String adminEmail;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
