package com.unigear.tracker.features.request.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AdminEquipmentRequestView {
    Long getId();
    Long getUserId();
    String getRequesterName();
    String getRequesterEmail();
    String getEquipmentName();
    String getCategory();
    String getDescription();
    Integer getQuantity();
    LocalDate getBorrowDate();
    LocalDate getReturnDate();
    String getStudentName();
    String getSchoolIdNumber();
    String getYearLevel();
    String getCourse();
    String getStatus();
    String getNotes();
    Boolean getReturnedOnTime();
    LocalDateTime getActualReturnedAt();
    String getEventApprovalPdf();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}