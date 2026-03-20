package com.unigear.tracker.controller;

import com.unigear.tracker.dto.ProfileDto;
import com.unigear.tracker.dto.UpdateProfileDto;
import com.unigear.tracker.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {
    
    @Autowired
    private ProfileService profileService;
    
    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            ProfileDto profile = profileService.getProfile(email);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateProfileDto dto,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            ProfileDto profile = profileService.updateProfile(email, dto);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    private String getUserEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        // If principal is our User entity (set by JwtAuthenticationFilter)
        if (principal instanceof com.unigear.tracker.entity.User) {
            return ((com.unigear.tracker.entity.User) principal).getEmail();
        }
        
        // If principal is UserDetails (from OAuth2 or login)
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        // Fallback to authentication name
        return authentication.getName();
    }
}
