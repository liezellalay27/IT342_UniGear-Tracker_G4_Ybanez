package com.unigear.tracker.pattern.factory;

import com.unigear.tracker.entity.Equipment;

/**
 * Factory Method Pattern Implementation: EquipmentStatusFactory
 * 
 * Creates appropriate Equipment status based on business rules.
 * This centralizes status resolution logic in one place.
 * 
 * Benefits:
 * - Easy to add new status types or rules
 * - Status logic is reusable across the application
 * - Easy to unit test status logic
 * - Follows Open/Closed Principle (open for extension, closed for modification)
 */
public class EquipmentStatusFactory {
    
    /**
     * Determine equipment status based on quantity and explicit status
     */
    public static Equipment.EquipmentStatus createStatus(String statusText, Integer availableQuantity) {
        // If explicit status is provided, try to use it
        if (statusText != null && !statusText.isBlank()) {
            try {
                return Equipment.EquipmentStatus.valueOf(statusText.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Fall back to quantity-based logic
            }
        }
        
        // Use quantity-based default
        return createStatusFromQuantity(availableQuantity);
    }
    
    /**
     * Determine status based on available quantity
     */
    public static Equipment.EquipmentStatus createStatusFromQuantity(Integer availableQuantity) {
        if (availableQuantity == null || availableQuantity <= 0) {
            return Equipment.EquipmentStatus.IN_USE;
        }
        return Equipment.EquipmentStatus.AVAILABLE;
    }
    
    /**
     * Determine status after a borrow request is approved
     * (decreases available quantity)
     */
    public static Equipment.EquipmentStatus createStatusAfterBorrow(
            Integer totalQuantity,
            Integer currentAvailableQuantity,
            Integer borrowQuantity) {
        
        if (totalQuantity == null || currentAvailableQuantity == null) {
            return Equipment.EquipmentStatus.IN_USE;
        }
        
        int newAvailableQuantity = currentAvailableQuantity - borrowQuantity;
        
        if (newAvailableQuantity <= 0) {
            return Equipment.EquipmentStatus.IN_USE;
        } else {
            return Equipment.EquipmentStatus.AVAILABLE;
        }
    }
    
    /**
     * Determine status after equipment is returned
     * (increases available quantity)
     */
    public static Equipment.EquipmentStatus createStatusAfterReturn(
            Integer totalQuantity,
            Integer currentAvailableQuantity,
            Integer returnQuantity) {
        
        if (totalQuantity == null) {
            return Equipment.EquipmentStatus.AVAILABLE;
        }
        
        int newAvailableQuantity = Math.min(
            currentAvailableQuantity + returnQuantity,
            totalQuantity
        );
        
        if (newAvailableQuantity <= 0) {
            return Equipment.EquipmentStatus.IN_USE;
        } else {
            return Equipment.EquipmentStatus.AVAILABLE;
        }
    }
    
    /**
     * Determine status for maintenance
     */
    public static Equipment.EquipmentStatus createMaintenanceStatus() {
        return Equipment.EquipmentStatus.MAINTENANCE;
    }
}
