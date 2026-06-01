package com.unigear.tracker.features.equipment.controller;

import com.unigear.tracker.features.equipment.dto.CreateEquipmentDto;
import com.unigear.tracker.features.equipment.dto.EquipmentDto;
import com.unigear.tracker.features.equipment.service.EquipmentService;
import com.unigear.tracker.features.user.entity.User;
import com.unigear.tracker.pattern.strategy.StrategyFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<?> getAllEquipment(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "CONTAINS") String searchType,
            @RequestParam(defaultValue = "BY_NAME") String sortBy) {
        try {
            List<EquipmentDto> items = equipmentService.getAllEquipment(category, search, searchType, sortBy);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(equipmentService.getEquipmentById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        try {
            List<EquipmentDto> items = equipmentService.getAllEquipment(null, null);
            List<String> categories = items.stream()
                    .map(EquipmentDto::getCategory)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get available search and sort strategies
     * Useful for UI to display search/sort options
     */
    @GetMapping("/api/v1/strategies")
    public ResponseEntity<?> getAvailableStrategies() {
        try {
            Map<String, Object> strategies = new HashMap<>();
            strategies.put("searchStrategies", StrategyFactory.getAvailableSearchStrategies());
            strategies.put("sortStrategies", StrategyFactory.getAvailableSortStrategies());
            return ResponseEntity.ok(strategies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createEquipment(
            @Valid @RequestBody CreateEquipmentDto dto,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            EquipmentDto created = equipmentService.createEquipment(email, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody CreateEquipmentDto dto,
            Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            EquipmentDto updated = equipmentService.updateEquipment(email, id, dto);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEquipment(@PathVariable Long id, Authentication authentication) {
        try {
            String email = getUserEmail(authentication);
            equipmentService.deleteEquipment(email, id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (com.unigear.tracker.exceptions.ResourceConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String getUserEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getEmail();
        }

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return authentication.getName();
    }
}
