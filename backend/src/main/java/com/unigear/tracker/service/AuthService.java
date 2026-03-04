package com.unigear.tracker.service;

import com.unigear.tracker.dto.AuthResponse;
import com.unigear.tracker.dto.LoginRequest;
import com.unigear.tracker.dto.RegisterRequest;
import com.unigear.tracker.entity.User;
import com.unigear.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
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
        
        // Save to database
        User savedUser = userRepository.save(user);
        
        // Return response without password
        return new AuthResponse(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            "Registration successful"
        );
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
        
        // Return response without password
        return new AuthResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            "Login successful"
        );
    }
}
