package com.smartgarage.backend.dto;

import com.smartgarage.backend.model.PaymentMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateRequestDTO {
    private Double amount;
    private PaymentMethod method;
}
