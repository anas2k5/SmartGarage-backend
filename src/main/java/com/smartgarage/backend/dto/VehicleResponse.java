package com.smartgarage.backend.dto;

import java.time.LocalDateTime;

public class VehicleResponse {

    private Long id;
    private String plateNumber;
    private String make;
    private String model;
    private Long ownerId;
    private LocalDateTime createdAt;

    public VehicleResponse() {}

    public VehicleResponse(Long id, String plateNumber, String make, String model,
                           Long ownerId, LocalDateTime createdAt) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.make = make;
        this.model = model;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
