package com.smartgarage.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {

    private Long id;
    private String invoiceNumber;
    private Long bookingId;
    private Long paymentId;
    private Double totalAmount;
    private LocalDateTime invoiceDate;
}
