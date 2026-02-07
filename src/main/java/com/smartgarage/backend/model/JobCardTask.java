package com.smartgarage.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // 🔥 BREAK LOOP
    @ManyToOne
    @JoinColumn(name = "job_card_id")
    @JsonIgnore
    private JobCard jobCard;

    @Column(nullable = false)
    private String description;

    private Double hours;
    private Double cost;
}
