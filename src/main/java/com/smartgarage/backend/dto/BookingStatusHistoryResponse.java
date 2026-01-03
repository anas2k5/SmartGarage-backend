package com.smartgarage.backend.dto;

import com.smartgarage.backend.model.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatusHistoryResponse {

    private BookingStatus oldStatus;
    private BookingStatus newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
}
