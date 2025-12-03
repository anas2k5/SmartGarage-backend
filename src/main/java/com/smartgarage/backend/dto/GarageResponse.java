package com.smartgarage.backend.dto;

import java.time.OffsetDateTime;

/**
 * DTO returned to clients for garage data.
 * Notice it does NOT include owner sensitive data (only ownerId).
 */
public class GarageResponse {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private boolean active;
    private Long ownerId;
    private OffsetDateTime createdAt;

    public GarageResponse() {}

    public GarageResponse(Long id, String name, String address, String phone, boolean active, Long ownerId, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.active = active;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
