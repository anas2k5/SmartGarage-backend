package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.JobCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface JobCardRepository extends JpaRepository<JobCard, Long> {

    Optional<JobCard> findByBookingId(Long bookingId);

    List<JobCard> findByMechanicId(Long mechanicId);

    List<JobCard> findByBookingGarageId(Long garageId);
}
