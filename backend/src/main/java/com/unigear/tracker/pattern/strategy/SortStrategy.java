package com.unigear.tracker.pattern.strategy;

import com.unigear.tracker.entity.Equipment;
import java.util.List;

/**
 * Strategy Pattern: Sort Strategy Interface
 * 
 * Defines the contract for different sorting algorithms.
 * Implementations can be:
 * - NameSortStrategy: Sort by name
 * - CategorySortStrategy: Sort by category then name
 * - AvailabilitySortStrategy: Sort by availability status
 * - DateAddedSortStrategy: Sort by creation date
 */
public interface SortStrategy {
    /**
     * Sort equipment based on the strategy
     * @param equipment list of equipment to sort
     * @return sorted equipment
     */
    List<Equipment> sort(List<Equipment> equipment);
    
    /**
     * Get strategy name
     */
    String getStrategyName();
}
