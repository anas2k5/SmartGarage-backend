package com.smartgarage.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who did it
    private Long actorId;
    private String actorEmail;
    private String actorRole;

    // What was changed
    private String action;       // STATUS_CHANGE, COST_UPDATE, PAYMENT_SUCCESS, ASSIGN_MECHANIC
    private String entityType;  // BOOKING, PAYMENT, GARAGE, MECHANIC
    private Long entityId;

    @Column(length = 2000)
    private String oldValue;

    @Column(length = 2000)
    private String newValue;

    private LocalDateTime timestamp;
}
