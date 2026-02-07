package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.JobCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface JobCardRepository
        extends JpaRepository<JobCard, Long> {

    // ================= BOOKING =================
    Optional<JobCard> findByBookingId(Long bookingId);

    // ================= MECHANIC =================
    List<JobCard> findByMechanicId(Long mechanicId);

    // ================= GARAGE =================
    @Query("""
        SELECT jc FROM JobCard jc
        JOIN FETCH jc.booking b
        JOIN FETCH b.garage g
        WHERE g.id = :garageId
    """)
    List<JobCard> findByGarageId(Long garageId);
}
