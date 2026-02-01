package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.JobCardPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobCardPartRepository
        extends JpaRepository<JobCardPart, Long> {

    List<JobCardPart> findByJobCardId(Long jobCardId);
}
