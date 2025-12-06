package com.smartgarage.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmRequestDTO {
    private String transactionId;
    private Double amountPaid;
    private boolean success;
}
