package com.unigear.tracker.pattern.factory.validators;

import com.unigear.tracker.pattern.factory.interfaces.RequestValidator;
import java.time.LocalDate;

/**
 * Factory Method Pattern: Concrete Validator for Date Validation
 * Validates borrow and return dates
 */
public class DateValidator implements RequestValidator {
    
    private final LocalDate borrowDate;
    private final LocalDate returnDate;
    
    public DateValidator(LocalDate borrowDate, LocalDate returnDate) {
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }
    
    @Override
    public void validate() {
        if (borrowDate == null) {
            throw new IllegalArgumentException("Borrow date is required");
        }
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date is required");
        }
        if (borrowDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Borrow date cannot be in the past");
        }
        if (returnDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return date must be on or after borrow date");
        }
    }
    
    @Override
    public String getValidatorName() {
        return "DateValidator";
    }
}
