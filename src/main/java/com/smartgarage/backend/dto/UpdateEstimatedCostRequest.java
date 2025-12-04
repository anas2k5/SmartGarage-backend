package com.smartgarage.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEstimatedCostRequest {
    @NotNull(message = "estimatedCost is required")
    private Double estimatedCost;
}
