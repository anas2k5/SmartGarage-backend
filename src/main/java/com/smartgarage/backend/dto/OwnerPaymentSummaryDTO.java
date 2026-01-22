package com.smartgarage.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerPaymentSummaryDTO {

    private Long paymentId;
    private Long bookingId;

    private String garageName;
    private String customerEmail;

    private Double amount;
    private String method;
    private String status;

    private LocalDateTime paidAt;
}
