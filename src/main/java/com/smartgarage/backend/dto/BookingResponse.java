package com.smartgarage.backend.dto;

import java.time.LocalDateTime;

public class BookingResponse {

    private Long id;

    private Long garageId;
    private String garageName;

    private Long customerId;
    private String customerEmail;

    private Long vehicleId;

    private String serviceType;
    private LocalDateTime bookingTime;
    private String status;
    private String details;

    public BookingResponse() {}

    public BookingResponse(Long id, Long garageId, String garageName,
                           Long customerId, String customerEmail,
                           Long vehicleId, String serviceType, LocalDateTime bookingTime,
                           String status, String details) {
        this.id = id;
        this.garageId = garageId;
        this.garageName = garageName;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.bookingTime = bookingTime;
        this.status = status;
        this.details = details;
    }

    // GETTERS AND SETTERS BELOW

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGarageId() { return garageId; }
    public void setGarageId(Long garageId) { this.garageId = garageId; }

    public String getGarageName() { return garageName; }
    public void setGarageName(String garageName) { this.garageName = garageName; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
