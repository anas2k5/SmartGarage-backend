package com.smartgarage.backend.dto;

import com.smartgarage.backend.model.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBookingSummaryDTO {

    private Long bookingId;
    private String garageName;
    private String serviceType;
    private BookingStatus status;
    private LocalDateTime bookingTime;
    private Double finalCost;
}
