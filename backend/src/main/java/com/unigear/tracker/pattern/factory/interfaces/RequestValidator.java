package com.unigear.tracker.pattern.factory.interfaces;

/**
 * Base interface for all validators
 * Factory Method Pattern: This interface defines the contract for all validators
 */
public interface RequestValidator {
    /**
     * Validate the input
     * @throws IllegalArgumentException if validation fails
     */
    void validate();
    
    /**
     * Get validator name for logging
     */
    String getValidatorName();
}
