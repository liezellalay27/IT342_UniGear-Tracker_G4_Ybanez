package com.unigear.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private Long id;
    private String name;
    private String email;
    private String message;
    private String accessToken;
    
    // Constructor for success responses
    public AuthResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Constructor for success responses with token
    public AuthResponse(Long id, String name, String email, String accessToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.accessToken = accessToken;
    }
    
    // Constructor for message-only responses
    public AuthResponse(String message) {
        this.message = message;
    }
}
