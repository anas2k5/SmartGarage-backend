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
    private String ownerName;

    private Long totalMechanics;

    private Double totalRevenue;
    private long activeGarages;

    // Existing
    private List<OwnerBookingSummaryDTO> recentBookings;

    // ✅ REAL PAYMENTS
    private List<OwnerPaymentSummaryDTO> recentPayments;
}
