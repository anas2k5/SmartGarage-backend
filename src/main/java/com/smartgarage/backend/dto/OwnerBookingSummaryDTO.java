package com.smartgarage.backend.dto;

import com.smartgarage.backend.model.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerBookingSummaryDTO {

    private Long bookingId;
    private Long customerId;
    private String customerEmail;
    private Long garageId;
    private String garageName;
    private String serviceType;
    private BookingStatus status;
    private LocalDateTime bookingTime;
    private Double finalCost;
}
