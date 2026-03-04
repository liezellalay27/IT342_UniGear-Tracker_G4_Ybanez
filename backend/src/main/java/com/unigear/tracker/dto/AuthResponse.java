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
    
    // Constructor for success responses
    public AuthResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Constructor for message-only responses
    public AuthResponse(String message) {
        this.message = message;
    }
}
