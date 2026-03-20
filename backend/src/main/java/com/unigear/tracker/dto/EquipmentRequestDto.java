package com.unigear.tracker.dto;

import com.unigear.tracker.entity.EquipmentRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequestDto {
    private Long id;
    private String equipmentName;
    private String category;
    private String description;
    private Integer quantity;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static EquipmentRequestDto fromEntity(EquipmentRequest request) {
        return new EquipmentRequestDto(
            request.getId(),
            request.getEquipmentName(),
            request.getCategory(),
            request.getDescription(),
            request.getQuantity(),
            request.getStatus().name(),
            request.getNotes(),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }
}
