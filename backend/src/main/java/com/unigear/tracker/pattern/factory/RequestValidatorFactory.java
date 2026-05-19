package com.unigear.tracker.pattern.factory;

import com.unigear.tracker.pattern.factory.interfaces.RequestValidator;
import com.unigear.tracker.pattern.factory.validators.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory Method Pattern Implementation: RequestValidatorFactory
 * 
 * Creates appropriate validators for equipment requests without exposing
 * the concrete validator classes to the client code.
 * 
 * Benefits:
 * - Easy to add new validator types without changing existing code
 * - Validators are decoupled from business logic
 * - Centralized validation logic
 * - Easy to test individual validators
 */
public class RequestValidatorFactory {
    
    /**
     * Create and return all validators needed for a request
     */
    public static List<RequestValidator> createRequestValidators(
            LocalDate borrowDate,
            LocalDate returnDate,
            String studentName,
            String schoolIdNumber,
            String yearLevel,
            String course,
            MultipartFile eventApprovalPdf) {
        
        List<RequestValidator> validators = new ArrayList<>();
        
        // Add date validator
        validators.add(new DateValidator(borrowDate, returnDate));
        
        // Add student info validator
        validators.add(new StudentInfoValidator(studentName, schoolIdNumber, yearLevel, course));
        
        // Add PDF file validator
        validators.add(new PdfFileValidator(eventApprovalPdf));
        
        return validators;
    }
    
    /**
     * Create only date validators
     */
    public static RequestValidator createDateValidator(LocalDate borrowDate, LocalDate returnDate) {
        return new DateValidator(borrowDate, returnDate);
    }
    
    /**
     * Create only student info validator
     */
    public static RequestValidator createStudentInfoValidator(
            String studentName,
            String schoolIdNumber,
            String yearLevel,
            String course) {
        return new StudentInfoValidator(studentName, schoolIdNumber, yearLevel, course);
    }
    
    /**
     * Create only PDF file validator
     */
    public static RequestValidator createPdfValidator(MultipartFile file) {
        return new PdfFileValidator(file);
    }
    
    /**
     * Execute all validators and collect errors
     * @throws IllegalArgumentException with all validation errors
     */
    public static void validateAll(List<RequestValidator> validators) {
        StringBuilder errors = new StringBuilder();
        
        for (RequestValidator validator : validators) {
            try {
                validator.validate();
            } catch (IllegalArgumentException e) {
                if (errors.length() > 0) {
                    errors.append("; ");
                }
                errors.append(validator.getValidatorName()).append(": ").append(e.getMessage());
            }
        }
        
        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString());
        }
    }
}
