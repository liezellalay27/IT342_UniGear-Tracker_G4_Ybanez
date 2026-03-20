package com.unigear.tracker.controller;

import com.unigear.tracker.dto.CreateRequestDto;
import com.unigear.tracker.dto.EquipmentRequestDto;
import com.unigear.tracker.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class RequestController {
    
    @Autowired
    private RequestService requestService;
    
    @PostMapping
    public ResponseEntity<?> createRequest(
            @Valid @RequestBody CreateRequestDto dto,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            EquipmentRequestDto request = requestService.createRequest(email, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getUserRequests(Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            List<EquipmentRequestDto> requests = requestService.getUserRequests(email);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            EquipmentRequestDto request = requestService.getRequestById(id, email);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            requestService.deleteRequest(id, email);
            return ResponseEntity.ok("Request deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    private String getUserEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication.getName();
    }
}
