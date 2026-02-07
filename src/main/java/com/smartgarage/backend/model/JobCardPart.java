package com.smartgarage.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // 🔥 BREAK LOOP
    @ManyToOne
    @JoinColumn(name = "job_card_id")
    @JsonIgnore
    private JobCard jobCard;

    @Column(nullable = false)
    private String name;

    private Integer quantity;
    private Double unitPrice;
}
