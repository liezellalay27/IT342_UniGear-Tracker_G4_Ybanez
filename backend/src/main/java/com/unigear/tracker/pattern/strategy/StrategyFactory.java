package com.unigear.tracker.pattern.strategy;

import com.unigear.tracker.pattern.strategy.search.ExactMatchSearchStrategy;
import com.unigear.tracker.pattern.strategy.search.ContainsSearchStrategy;
import com.unigear.tracker.pattern.strategy.sort.NameSortStrategy;
import com.unigear.tracker.pattern.strategy.sort.CategorySortStrategy;
import com.unigear.tracker.pattern.strategy.sort.AvailabilitySortStrategy;
import java.util.*;

/**
 * Strategy Pattern: Strategy Factory
 * 
 * Creates appropriate search and sort strategy instances.
 * Makes it easy to add new strategies without modifying client code.
 */
public class StrategyFactory {
    
    public enum SearchType {
        EXACT_MATCH,
        CONTAINS,
        FULL_TEXT  // Future implementation
    }
    
    public enum SortType {
        BY_NAME,
        BY_CATEGORY,
        BY_AVAILABILITY,
        BY_DATE  // Future implementation
    }
    
    /**
     * Create search strategy by type
     */
    public static SearchStrategy createSearchStrategy(SearchType type) {
        switch (type) {
            case EXACT_MATCH:
                return new ExactMatchSearchStrategy();
            case CONTAINS:
                return new ContainsSearchStrategy();
            case FULL_TEXT:
                // TODO: Implement full-text search
                return new ContainsSearchStrategy();
            default:
                throw new IllegalArgumentException("Unknown search type: " + type);
        }
    }
    
    /**
     * Create sort strategy by type
     */
    public static SortStrategy createSortStrategy(SortType type) {
        switch (type) {
            case BY_NAME:
                return new NameSortStrategy();
            case BY_CATEGORY:
                return new CategorySortStrategy();
            case BY_AVAILABILITY:
                return new AvailabilitySortStrategy();
            case BY_DATE:
                // TODO: Implement date sort
                return new NameSortStrategy();
            default:
                throw new IllegalArgumentException("Unknown sort type: " + type);
        }
    }
    
    /**
     * Create search strategy from string
     */
    public static SearchStrategy createSearchStrategy(String type) {
        try {
            return createSearchStrategy(SearchType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // Default to contains search
            return new ContainsSearchStrategy();
        }
    }
    
    /**
     * Create sort strategy from string
     */
    public static SortStrategy createSortStrategy(String type) {
        try {
            return createSortStrategy(SortType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // Default to name sort
            return new NameSortStrategy();
        }
    }
    
    /**
     * Get all available search strategies for UI
     */
    public static List<Map<String, String>> getAvailableSearchStrategies() {
        List<Map<String, String>> strategies = new ArrayList<>();
        for (SearchType type : SearchType.values()) {
            Map<String, String> strategy = new HashMap<>();
            strategy.put("value", type.name());
            strategy.put("label", getSearchStrategyLabel(type));
            strategies.add(strategy);
        }
        return strategies;
    }
    
    /**
     * Get all available sort strategies for UI
     */
    public static List<Map<String, String>> getAvailableSortStrategies() {
        List<Map<String, String>> strategies = new ArrayList<>();
        for (SortType type : SortType.values()) {
            Map<String, String> strategy = new HashMap<>();
            strategy.put("value", type.name());
            strategy.put("label", getSortStrategyLabel(type));
            strategies.add(strategy);
        }
        return strategies;
    }
    
    /**
     * Get human-readable label for search strategy
     */
    private static String getSearchStrategyLabel(SearchType type) {
        switch (type) {
            case EXACT_MATCH:
                return "Exact Match";
            case CONTAINS:
                return "Contains";
            case FULL_TEXT:
                return "Full Text Search";
            default:
                return type.name();
        }
    }
    
    /**
     * Get human-readable label for sort strategy
     */
    private static String getSortStrategyLabel(SortType type) {
        switch (type) {
            case BY_NAME:
                return "Sort by Name";
            case BY_CATEGORY:
                return "Sort by Category";
            case BY_AVAILABILITY:
                return "Sort by Availability";
            case BY_DATE:
                return "Sort by Date";
            default:
                return type.name();
        }
    }
}
