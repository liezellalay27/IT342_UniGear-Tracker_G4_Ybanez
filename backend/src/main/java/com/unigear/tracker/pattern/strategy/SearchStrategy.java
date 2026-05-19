package com.unigear.tracker.pattern.strategy;

import com.unigear.tracker.entity.Equipment;
import java.util.List;

/**
 * Strategy Pattern: Search Strategy Interface
 * 
 * Defines the contract for different search algorithms.
 * Implementations can be:
 * - ExactMatchSearchStrategy: Find exact matches only
 * - FuzzySearchStrategy: Find similar matches
 * - FullTextSearchStrategy: Advanced full-text search
 */
public interface SearchStrategy {
    /**
     * Search equipment based on the strategy
     * @param equipment list of equipment to search through
     * @param query search query
     * @return filtered equipment matching the query
     */
    List<Equipment> search(List<Equipment> equipment, String query);
    
    /**
     * Get strategy name
     */
    String getStrategyName();
}
