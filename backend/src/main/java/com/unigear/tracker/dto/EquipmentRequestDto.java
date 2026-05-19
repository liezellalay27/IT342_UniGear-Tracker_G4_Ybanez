package com.unigear.tracker.dto;

import com.unigear.tracker.entity.EquipmentRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequestDto {
    private Long id;
    private Long userId;
    private String requesterName;
    private String requesterEmail;
    private String equipmentName;
    private String category;
    private String description;
    private Integer quantity;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private String studentName;
    private String schoolIdNumber;
    private String yearLevel;
    private String course;
    private String status;
    private String notes;
    private Boolean returnedOnTime;
    private LocalDateTime actualReturnedAt;
    private String eventApprovalPdf;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static EquipmentRequestDto fromEntity(EquipmentRequest request) {
        return new EquipmentRequestDto(
            request.getId(),
            request.getUser() != null ? request.getUser().getId() : null,
            request.getUser() != null ? request.getUser().getName() : null,
            request.getUser() != null ? request.getUser().getEmail() : null,
            request.getEquipmentName(),
            request.getCategory(),
            request.getDescription(),
            request.getQuantity(),
            request.getBorrowDate(),
            request.getReturnDate(),
            request.getStudentName(),
            request.getSchoolIdNumber(),
            request.getYearLevel(),
            request.getCourse(),
            request.getStatus().name(),
            request.getNotes(),
            request.getReturnedOnTime(),
            request.getActualReturnedAt(),
            request.getEventApprovalPdfFilename() != null ? "pdf" : null,
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }
}
