package com.smartgarage.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * BookingRequest used by controller. client should send:
 * - garageId
 * - vehicleId
 * - serviceType (optional)
 * - bookingTime (ISO8601) -> parsed by controller / Jackson to LocalDateTime
 * - details (optional)
 *
 * customerId will be set server-side (from Principal) before passing to service.
 */
public class BookingRequest {

    @NotNull
    private Long garageId;

    @NotNull
    private Long vehicleId;

    private String serviceType;

    @NotNull
    private LocalDateTime bookingTime;

    private String details;

    /**
     * This is set by the server (controller) after authentication.
     * Stored here temporarily so service can validate ownership.
     */
    @JsonIgnore
    private Long customerId;

    public BookingRequest() {}

    public Long getGarageId() { return garageId; }
    public void setGarageId(Long garageId) { this.garageId = garageId; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
}
