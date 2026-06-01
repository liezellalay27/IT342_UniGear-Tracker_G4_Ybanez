package com.unigear.tracker.features.request.controller;

import com.unigear.tracker.features.request.dto.CreateRequestDto;
import com.unigear.tracker.features.request.dto.EquipmentRequestDto;
import com.unigear.tracker.features.request.service.RequestService;
import com.unigear.tracker.features.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/requests")
@CrossOrigin(origins = "*")
public class RequestController {
    
    @Autowired
    private RequestService requestService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createRequest(
            @RequestParam String equipmentName,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam Integer quantity,
            @RequestParam LocalDate borrowDate,
            @RequestParam LocalDate returnDate,
            @RequestParam String studentName,
            @RequestParam String schoolIdNumber,
            @RequestParam String yearLevel,
            @RequestParam String course,
            @RequestPart(required = false) MultipartFile eventApprovalPdf,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            CreateRequestDto dto = new CreateRequestDto(
                equipmentName, category, description, quantity,
                borrowDate, returnDate, studentName, schoolIdNumber,
                yearLevel, course
            );
            EquipmentRequestDto request = requestService.createRequest(email, dto, eventApprovalPdf);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (Exception e) {
            return buildErrorResponse(e);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getUserRequests(Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            List<EquipmentRequestDto> requests = requestService.getUserRequests(email);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return buildErrorResponse(e);
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
            return buildErrorResponse(e);
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
            return buildErrorResponse(e);
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> downloadPdf(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            byte[] pdfContent = requestService.getPdfContent(id, email);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=event_approval.pdf")
                    .header("Content-Type", "application/pdf")
                    .body(pdfContent);
        } catch (Exception e) {
            return buildErrorResponse(e);
        }
    }

    private ResponseEntity<String> buildErrorResponse(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            Throwable cause = e.getCause();
            message = cause != null ? cause.getMessage() : null;
        }
        if (message == null || message.isBlank()) {
            message = "Request processing failed";
        }

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String lower = message.toLowerCase();
        if (lower.contains("unauthorized") || lower.contains("forbidden")) {
            status = HttpStatus.FORBIDDEN;
        } else if (lower.contains("exceed") || lower.contains("size") || lower.contains("too large")) {
            status = HttpStatus.PAYLOAD_TOO_LARGE;
        }

        return ResponseEntity.status(status).body(message);
    }
    
    private String getUserEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getEmail();
        }

        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication.getName();
    }
}
