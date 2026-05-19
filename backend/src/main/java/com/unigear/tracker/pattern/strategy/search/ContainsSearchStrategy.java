package com.unigear.tracker.pattern.strategy.search;

import com.unigear.tracker.entity.Equipment;
import com.unigear.tracker.pattern.strategy.SearchStrategy;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Pattern: Concrete Search Strategy
 * 
 * Performs case-insensitive substring search.
 * Most commonly used search strategy for general-purpose queries.
 */
public class ContainsSearchStrategy implements SearchStrategy {
    
    @Override
    public List<Equipment> search(List<Equipment> equipment, String query) {
        if (query == null || query.isBlank()) {
            return equipment;
        }
        
        String lowerQuery = query.toLowerCase();
        
        return equipment.stream()
            .filter(e -> e.getName().toLowerCase().contains(lowerQuery) ||
                        e.getCategory().toLowerCase().contains(lowerQuery) ||
                        e.getDescription().toLowerCase().contains(lowerQuery))
            .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "Contains";
    }
}
