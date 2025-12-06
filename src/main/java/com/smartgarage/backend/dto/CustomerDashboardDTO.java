package com.smartgarage.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDashboardDTO {

    private Long customerId;

    private long totalBookings;
    private long completedBookings;
    private long ongoingBookings;   // e.g. IN_PROGRESS
    private long pendingBookings;   // PENDING
    private long cancelledBookings;

    private Double totalSpent;      // sum of successful payments

    private List<CustomerBookingSummaryDTO> latestBookings;
}
