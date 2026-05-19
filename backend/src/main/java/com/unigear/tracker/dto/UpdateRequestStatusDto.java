package com.unigear.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRequestStatusDto {
    @NotBlank(message = "Status is required")
    private String status;

    private String notes;

    private Boolean returnedOnTime;
}
