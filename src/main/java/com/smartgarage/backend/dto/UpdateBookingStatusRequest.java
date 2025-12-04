package com.smartgarage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateBookingStatusRequest {

    @NotBlank(message = "status is required")
    @Pattern(regexp = "PENDING|ACCEPTED|IN_PROGRESS|COMPLETED|CANCELLED",
            message = "status must be one of PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED")
    private String status;

    public UpdateBookingStatusRequest() {}

    public UpdateBookingStatusRequest(String status) { this.status = status; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
