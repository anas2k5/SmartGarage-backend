package com.smartgarage.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plateNumber;
    private String make;
    private String model;

    // owner relationship (many vehicles to one user)
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
