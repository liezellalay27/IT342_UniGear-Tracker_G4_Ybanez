package com.unigear.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String equipmentName;
    
    @Column(nullable = false)
    private String category;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "borrow_date")
    private LocalDate borrowDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "school_id_number")
    private String schoolIdNumber;

    @Column(name = "year_level")
    private String yearLevel;

    @Column(name = "course")
    private String course;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(length = 500)
    private String notes;

    @Column(name = "returned_on_time")
    private Boolean returnedOnTime;

    @Column(name = "actual_returned_at")
    private LocalDateTime actualReturnedAt;

    @Lob
    @Column(name = "event_approval_pdf")
    private byte[] eventApprovalPdf;

    @Column(name = "event_approval_pdf_filename")
    private String eventApprovalPdfFilename;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED,
        COMPLETED
    }
}
