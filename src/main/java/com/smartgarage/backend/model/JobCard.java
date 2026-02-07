package com.smartgarage.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class JobCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= BOOKING =================
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    @JsonIgnoreProperties({
            "garage",
            "customer",
            "vehicle",
            "service"
    }) // ✅ mechanic removed from ignore
    private Booking booking;

    // ================= MECHANIC =================
    @ManyToOne
    @JoinColumn(name = "mechanic_id", nullable = false)
    @JsonIgnoreProperties({
            "garage",
            "user"
    })
    private Mechanic mechanic;

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobCardStatus status;

    // ================= COST =================
    private Double laborCost;
    private Double partsCost;
    private Double totalCost; // ✅ ADDED

    @Column(length = 2000)
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    // ================= TASKS =================
    @OneToMany(
            mappedBy = "jobCard",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<JobCardTask> tasks;

    // ================= PARTS =================
    @OneToMany(
            mappedBy = "jobCard",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<JobCardPart> parts;

    // ================= TOTAL COST =================
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}
