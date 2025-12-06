package com.smartgarage.backend.dto;

import com.smartgarage.backend.model.PaymentMethod;
import com.smartgarage.backend.model.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private Long id;
    private Long bookingId;
    private Double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
}
