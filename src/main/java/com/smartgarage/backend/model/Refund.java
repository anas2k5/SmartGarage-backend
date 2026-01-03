package com.smartgarage.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    @JsonIgnore // 🚨 IMPORTANT (prevents ByteBuddy error)
    private Payment payment;

    private Double amount;

    private String reason;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private LocalDateTime refundedAt;
}
