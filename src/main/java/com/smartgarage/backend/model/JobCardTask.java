package com.smartgarage.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_card_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCardTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_card_id")
    private JobCard jobCard;

    @Column(nullable = false)
    private String description;

    private Double hours;
    private Double cost;
}
