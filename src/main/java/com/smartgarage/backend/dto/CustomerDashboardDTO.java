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
    private String customerName;

    // 🔥 ADD THIS
    private VehicleDTO primaryVehicle;

    private long totalBookings;
    private long completedBookings;
    private long ongoingBookings;
    private long pendingBookings;
    private long cancelledBookings;

    private Double totalSpent;

    private List<CustomerBookingSummaryDTO> latestBookings;
}
