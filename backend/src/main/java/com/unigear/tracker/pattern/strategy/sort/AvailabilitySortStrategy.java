package com.unigear.tracker.pattern.strategy.sort;

import com.unigear.tracker.entity.Equipment;
import com.unigear.tracker.pattern.strategy.SortStrategy;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Pattern: Concrete Sort Strategy
 * 
 * Sorts equipment by availability status.
 * Order: AVAILABLE > LIMITED > IN_USE > MAINTENANCE > RETIRED
 */
public class AvailabilitySortStrategy implements SortStrategy {
    
    @Override
    public List<Equipment> sort(List<Equipment> equipment) {
        return equipment.stream()
            .sorted((a, b) -> {
                // Define priority order
                int priorityA = getStatusPriority(a.getStatus());
                int priorityB = getStatusPriority(b.getStatus());
                
                if (priorityA != priorityB) {
                    return Integer.compare(priorityA, priorityB);
                }
                // If same status, sort by name
                return a.getName().compareToIgnoreCase(b.getName());
            })
            .collect(Collectors.toList());
    }
    
    private int getStatusPriority(Equipment.EquipmentStatus status) {
        switch (status) {
            case AVAILABLE:
                return 1;
            case IN_USE:
                return 2;
            case MAINTENANCE:
                return 3;
            default:
                return 4;
        }
    }
    
    @Override
    public String getStrategyName() {
        return "ByAvailability";
    }
}
