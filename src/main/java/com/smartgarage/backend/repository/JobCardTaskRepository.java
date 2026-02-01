package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.JobCardTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobCardTaskRepository
        extends JpaRepository<JobCardTask, Long> {

    List<JobCardTask> findByJobCardId(Long jobCardId);
}
