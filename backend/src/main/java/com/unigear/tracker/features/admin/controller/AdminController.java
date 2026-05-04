package com.unigear.tracker.controller;

import com.unigear.tracker.dto.EquipmentRequestDto;
import com.unigear.tracker.dto.UpdateRequestStatusDto;
import com.unigear.tracker.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private RequestService requestService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            return ResponseEntity.ok(requestService.getAllUsersForAdmin(email));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests(Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            return ResponseEntity.ok(requestService.getAllRequestsForAdmin(email));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/borrowed")
    public ResponseEntity<?> getBorrowedRecords(Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            return ResponseEntity.ok(requestService.getBorrowedRequestsForAdmin(email));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<?> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestStatusDto dto,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            EquipmentRequestDto updated = requestService.updateRequestStatusForAdmin(
                    email,
                    id,
                    dto.getStatus(),
                    dto.getNotes(),
                    dto.getReturnedOnTime()
            );
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String getUserEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof com.unigear.tracker.entity.User) {
            return ((com.unigear.tracker.entity.User) principal).getEmail();
        }

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return authentication.getName();
    }
}
