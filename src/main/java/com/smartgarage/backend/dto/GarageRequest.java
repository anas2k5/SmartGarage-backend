package com.smartgarage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO used for create/update garage requests.
 */
public class GarageRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 1000)
    private String address;

    @NotBlank(message = "Phone is required")
    @Size(max = 50)
    private String phone;

    public GarageRequest() {}

    public GarageRequest(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    // getters / setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
