package com.smartgarage.backend.service;

import com.smartgarage.backend.model.JobCard;
import com.smartgarage.backend.model.JobCardPart;
import com.smartgarage.backend.model.JobCardTask;

import java.util.List;

public interface JobCardService {

    // ================= FETCH =================

    List<JobCard> getByMechanic(Long mechanicId);

    // ================= TASK =================

    JobCardTask addTask(
            Long jobCardId,
            String description,
            Double hours,
            Double cost
    );

    // ================= PART =================

    JobCardPart addPart(
            Long jobCardId,
            String name,
            Integer quantity,
            Double unitPrice
    );

    // ================= STATUS =================

    JobCard approveJob(Long jobCardId);

    JobCard closeJob(Long jobCardId);


    List<JobCard> getByGarage(Long garageId);
}
