package com.smartgarage.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_card_parts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCardPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_card_id")
    private JobCard jobCard;

    @Column(nullable = false)
    private String name;

    private Integer quantity;
    private Double unitPrice;
}
