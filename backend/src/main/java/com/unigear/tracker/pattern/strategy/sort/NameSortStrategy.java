package com.unigear.tracker.pattern.strategy.sort;

import com.unigear.tracker.entity.Equipment;
import com.unigear.tracker.pattern.strategy.SortStrategy;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Pattern: Concrete Sort Strategy
 * 
 * Sorts equipment by name in case-insensitive order.
 */
public class NameSortStrategy implements SortStrategy {
    
    @Override
    public List<Equipment> sort(List<Equipment> equipment) {
        return equipment.stream()
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "ByName";
    }
}
