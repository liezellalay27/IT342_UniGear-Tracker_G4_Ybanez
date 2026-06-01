package com.unigear.tracker.features.equipment.dto;

import com.unigear.tracker.features.equipment.entity.Equipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDto {

    private Long id;
    private String name;
    private String category;
    private String location;
    private String description;
    private List<String> specifications;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }

        return Arrays.stream(category.trim().toLowerCase().replace('_', ' ').split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static EquipmentDto fromEntity(Equipment equipment) {
        List<String> specs = Arrays.stream(equipment.getSpecifications().split("\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        return new EquipmentDto(
                equipment.getId(),
                equipment.getName(),
                normalizeCategory(equipment.getCategory()),
                equipment.getLocation(),
                equipment.getDescription(),
                specs,
                equipment.getTotalQuantity(),
                equipment.getAvailableQuantity(),
                equipment.getStatus().name(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
    }
}
