package com.unigear.tracker.pattern.strategy.sort;

import com.unigear.tracker.entity.Equipment;
import com.unigear.tracker.pattern.strategy.SortStrategy;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Pattern: Concrete Sort Strategy
 * 
 * Sorts equipment first by category, then by name within each category.
 */
public class CategorySortStrategy implements SortStrategy {
    
    @Override
    public List<Equipment> sort(List<Equipment> equipment) {
        return equipment.stream()
            .sorted((a, b) -> {
                // First compare by category
                int categoryCompare = a.getCategory().compareToIgnoreCase(b.getCategory());
                if (categoryCompare != 0) {
                    return categoryCompare;
                }
                // If same category, sort by name
                return a.getName().compareToIgnoreCase(b.getName());
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "ByCategory";
    }
}
