package com.smartgarage.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "garages")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "max_bookings_per_slot")
    private Integer maxBookingsPerSlot = 3;
    private String name;
    private String address;
    private String phone;
    private boolean active = true;

    // ✅ ADD THESE 2 FIELDS
    private Double latitude;
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Garage() {}

    // Getters & Setters

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

    // ✅ NEW GETTERS/SETTERS
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getMaxBookingsPerSlot() {
        return maxBookingsPerSlot;
    }

    public void setMaxBookingsPerSlot(Integer maxBookingsPerSlot) {
        this.maxBookingsPerSlot = maxBookingsPerSlot;
    }
}
