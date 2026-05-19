package com.unigear.tracker.service;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.admin.email:admin@unigear.com}")
    private String adminEmail;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Register a new user
     * - Validates that email doesn't exist
     * - Hashes the password using BCrypt
     * - Saves user to database
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        // Create new user
        User user = new User(
            request.getName(),
            request.getEmail(),
            hashedPassword
        );
        user.setRole(resolveRoleForEmail(request.getEmail()));
        
        // Save to database
        User savedUser = userRepository.save(user);

        // Return response without password
        AuthResponse response = new AuthResponse(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getRole().name()
        );
        response.setMessage("Registration successful");
        return response;
    }
    
    /**
     * Login user
     * - Validates credentials
     * - Checks if user exists
     * - Verifies password using BCrypt
     */
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateJwtToken(user.getEmail());

        // Return response without password
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
     * Authenticate user with Google OAuth2
     * - Creates or updates user from OAuth2 data
     * - Generates JWT token
     */
    @Transactional
    public AuthResponse authenticateWithGoogleOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        
        if (email == null) {
            throw new IllegalArgumentException("Email not provided by OAuth2 provider");
        }
        
        // Find or create user
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update user info
            user.setName(name);
            user.setPicture(picture);
        } else {
            // Create new user for OAuth2
            // Generate a random password for OAuth2 users (they won't use it)
            String randomPassword = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
            
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPassword(randomPassword);
            user.setPicture(picture);
            user.setRole(resolveRoleForEmail(email));
            user = userRepository.save(user);
        }

        // Existing users are managed in this transaction and will flush on commit.
        
        // Generate JWT token
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
        boolean matchesConfiguredAdminEmail = email != null
                && adminEmail != null
                && email.equalsIgnoreCase(adminEmail);
        boolean adminAlreadyExists = userRepository.existsByRole(User.Role.ADMIN);

        if (matchesConfiguredAdminEmail && !adminAlreadyExists) {
            return User.Role.ADMIN;
        }
        return User.Role.STUDENT;
    }
}
