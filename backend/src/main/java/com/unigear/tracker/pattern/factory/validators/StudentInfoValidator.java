package com.unigear.tracker.pattern.factory.validators;

import com.unigear.tracker.pattern.factory.interfaces.RequestValidator;

import java.io.IOException;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Factory Method Pattern: Concrete Validator for Student Information
 * Validates required student fields
 */
public class StudentInfoValidator implements RequestValidator {
    
    private final String studentName;
    private final String schoolIdNumber;
    private final String yearLevel;
    private final String course;
    
    public StudentInfoValidator(String studentName, String schoolIdNumber, String yearLevel, String course) {
        this.studentName = studentName;
        this.schoolIdNumber = schoolIdNumber;
        this.yearLevel = yearLevel;
        this.course = course;
    }
    
    @Override
    public void validate() {
        if (studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("Student name is required");
        }
        if (schoolIdNumber == null || schoolIdNumber.isBlank()) {
            throw new IllegalArgumentException("School ID number is required");
        }
        if (!schoolIdNumber.matches("^\\d{2}-\\d{4}-\\d{3}$")) {
            throw new IllegalArgumentException("School ID must follow format XX-XXXX-XXX");
        }
        if (yearLevel == null || yearLevel.isBlank()) {
            throw new IllegalArgumentException("Year level is required");
        }
        if (course == null || course.isBlank()) {
            throw new IllegalArgumentException("Course is required");
        }
    }
    
    @Override
    public String getValidatorName() {
        return "StudentInfoValidator";
    }
}
