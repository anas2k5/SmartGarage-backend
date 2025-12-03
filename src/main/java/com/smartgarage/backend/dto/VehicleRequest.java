package com.smartgarage.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class VehicleRequest {

    @NotBlank(message = "plateNumber is required")
    private String plateNumber;

    private String make;
    private String model;

    public VehicleRequest() {}

    public VehicleRequest(String plateNumber, String make, String model) {
        this.plateNumber = plateNumber;
        this.make = make;
        this.model = model;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
