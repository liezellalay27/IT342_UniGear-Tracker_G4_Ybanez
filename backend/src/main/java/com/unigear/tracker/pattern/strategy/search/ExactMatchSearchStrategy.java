package com.unigear.tracker.pattern.strategy.search;

import com.unigear.tracker.entity.Equipment;
import com.unigear.tracker.pattern.strategy.SearchStrategy;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Pattern: Concrete Search Strategy
 * 
 * Performs exact match search on equipment name and category.
 */
public class ExactMatchSearchStrategy implements SearchStrategy {
    
    @Override
    public List<Equipment> search(List<Equipment> equipment, String query) {
        if (query == null || query.isBlank()) {
            return equipment;
        }
        
        String lowerQuery = query.toLowerCase();
        
        return equipment.stream()
            .filter(e -> e.getName().toLowerCase().equals(lowerQuery) ||
                        e.getCategory().toLowerCase().equals(lowerQuery))
            .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "ExactMatch";
    }
}
