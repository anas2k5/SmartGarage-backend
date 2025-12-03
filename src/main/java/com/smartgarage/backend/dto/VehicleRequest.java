package com.smartgarage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VehicleRequest {

    @NotBlank(message = "plateNumber is required")
    @Size(min = 2, max = 64, message = "plateNumber length must be between 2 and 64")
    @Pattern(regexp = "^[A-Z0-9\\- ]+$", message = "plateNumber must be uppercase alphanumeric, dashes or spaces")
    private String plateNumber;

    @NotBlank(message = "make is required")
    private String make;

    @NotBlank(message = "model is required")
    private String model;

    // getters & setters
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
