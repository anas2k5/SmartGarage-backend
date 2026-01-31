package com.smartgarage.backend.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {

    private long totalUsers;
    private long totalCustomers;
    private long totalOwners;
    private long totalGarages;
    private long totalBookings;

    private long pendingBookings;
    private long acceptedBookings;
    private long inProgressBookings;
    private long completedBookings;
    private long cancelledBookings;
    private long paidBookings;

    private Double totalRevenue;

    private List<OwnerBookingSummaryDTO> recentBookings;
    private List<OwnerPaymentSummaryDTO> recentPayments;
}
