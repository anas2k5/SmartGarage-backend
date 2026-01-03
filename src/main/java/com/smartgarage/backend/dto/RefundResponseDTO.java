package com.smartgarage.backend.dto;

import com.smartgarage.backend.model.RefundStatus;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundResponseDTO {

    private Long refundId;
    private Long bookingId;
    private Long paymentId;
    private Double amount;
    private String reason;
    private RefundStatus status;
    private LocalDateTime refundedAt;
}
