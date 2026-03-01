package com.smartgarage.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateSlotCapacityRequest {

    @NotNull
    @Min(1)
    private Integer maxBookingsPerSlot;

    public Integer getMaxBookingsPerSlot() {
        return maxBookingsPerSlot;
    }

    public void setMaxBookingsPerSlot(Integer maxBookingsPerSlot) {
        this.maxBookingsPerSlot = maxBookingsPerSlot;
    }
}