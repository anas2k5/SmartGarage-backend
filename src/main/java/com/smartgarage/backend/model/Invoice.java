package com.smartgarage.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @OneToOne
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    private String invoiceNumber;        // e.g. "INV-2025-0001"
    private LocalDateTime invoiceDate;
    private Double totalAmount;

    // later you can store PDF path / URL here
    private String pdfUrl;
}
