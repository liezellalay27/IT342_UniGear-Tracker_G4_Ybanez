package com.unigear.tracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDto {
    
    @NotBlank(message = "Equipment name is required")
    private String equipmentName;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    private String description;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Borrow date is required")
    private LocalDate borrowDate;

    @NotNull(message = "Return date is required")
    private LocalDate returnDate;

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "School ID number is required")
    @Pattern(regexp = "^\\d{2}-\\d{4}-\\d{3}$", message = "School ID must follow format 17-0635-488")
    private String schoolIdNumber;

    @NotBlank(message = "Year is required")
    private String yearLevel;

    @NotBlank(message = "Course is required")
    private String course;
}
