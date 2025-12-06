package com.smartgarage.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // which garage this booking belongs to
    @ManyToOne
    @JoinColumn(name = "garage_id")
    private Garage garage;

    // customer who created the booking
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    // vehicle being serviced
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    // e.g. "Engine Service", "Oil Change"
    private String serviceType;

    // when the booking was created / scheduled
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(length = 2000)
    private String details;

    // assigned mechanic (nullable until assigned)
    @ManyToOne
    @JoinColumn(name = "mechanic_id")
    private Mechanic mechanic;

    // owner / mechanic estimated cost before work
    private Double estimatedCost;

    // final cost after work completed
    private Double finalCost;
}
