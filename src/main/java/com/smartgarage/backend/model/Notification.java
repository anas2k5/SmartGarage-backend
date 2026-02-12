package com.smartgarage.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;   // Receiver

    private String title;

    @Column(length = 2000)
    private String message;

    private String type;   // BOOKING / PAYMENT / JOB_CARD

    private boolean readStatus = false;

    private LocalDateTime createdAt;
}
