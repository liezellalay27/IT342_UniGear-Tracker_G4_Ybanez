package com.unigear.tracker.features.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDto {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String picture;
}
