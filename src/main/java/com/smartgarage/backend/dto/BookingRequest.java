package com.smartgarage.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

/**
 * DTO used for creating bookings from the client.
 * Contains only simple fields / ids — nested objects are not sent in the request.
 */
public class BookingRequest {

    private Long garageId;
    private Long vehicleId;
    private Long customerId;        // optional: if controller sets authenticated user, this can be null
    private String serviceType;
    private String details;

    /**
     * Use OffsetDateTime for timezone-aware datetimes coming from client.
     * Example client value: "2025-12-01T20:30:38.445Z" or "2025-12-01T20:30:38.445+05:30"
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime slotStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime slotEnd;

    // --- Constructors ---
    public BookingRequest() {}

    public BookingRequest(Long garageId, Long vehicleId, Long customerId, String serviceType,
                          String details, OffsetDateTime slotStart, OffsetDateTime slotEnd) {
        this.garageId = garageId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.details = details;
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
    }

    // --- Getters & Setters ---
    public Long getGarageId() {
        return garageId;
    }

    public void setGarageId(Long garageId) {
        this.garageId = garageId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public OffsetDateTime getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(OffsetDateTime slotStart) {
        this.slotStart = slotStart;
    }

    public OffsetDateTime getSlotEnd() {
        return slotEnd;
    }

    public void setSlotEnd(OffsetDateTime slotEnd) {
        this.slotEnd = slotEnd;
    }

    @Override
    public String toString() {
        return "BookingRequest{" +
                "garageId=" + garageId +
                ", vehicleId=" + vehicleId +
                ", customerId=" + customerId +
                ", serviceType='" + serviceType + '\'' +
                ", details='" + details + '\'' +
                ", slotStart=" + slotStart +
                ", slotEnd=" + slotEnd +
                '}';
    }
}
