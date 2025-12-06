package com.smartgarage.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDashboardDTO {

    private Long ownerId;

    private long totalBookings;
    private long pendingBookings;
    private long inProgressBookings;
    private long acceptedBookings;
    private long completedBookings;
    private long cancelledBookings;

    private Double totalRevenue;      // sum of successful payments for all their garages
    private long activeGarages;       // number of garages that have at least 1 booking

    private List<OwnerBookingSummaryDTO> recentBookings;
}
