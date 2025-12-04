package com.smartgarage.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private Long garageId;
    private String garageName;

    private Long customerId;
    private String customerEmail;

    private Long vehicleId;
    private String vehiclePlate;

    private String serviceType;
    private LocalDateTime bookingTime;
    private String status;
    private String details;

    // mechanic info
    private Long mechanicId;
    private String mechanicName;
    private String mechanicPhone;

    // cost fields
    private Double estimatedCost;
    private Double finalCost;
}
