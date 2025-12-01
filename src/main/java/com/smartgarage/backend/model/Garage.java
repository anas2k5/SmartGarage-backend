package com.smartgarage.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "garages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Garage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String phone;
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
