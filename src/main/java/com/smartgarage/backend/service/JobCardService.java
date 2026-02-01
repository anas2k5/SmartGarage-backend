package com.smartgarage.backend.service;

import com.smartgarage.backend.model.JobCard;
import com.smartgarage.backend.model.JobCardPart;
import com.smartgarage.backend.model.JobCardTask;

import java.util.List;

public interface JobCardService {

    JobCard createJobCard(Long bookingId, Long mechanicId, String requesterEmail);

    List<JobCard> getByGarage(Long garageId);

    List<JobCard> getByMechanic(Long mechanicId);

    JobCardTask addTask(
            Long jobCardId,
            String description,
            Double hours,
            Double cost
    );

    JobCardPart addPart(
            Long jobCardId,
            String name,
            Integer quantity,
            Double unitPrice
    );

    JobCard approveJob(Long jobCardId);

    JobCard closeJob(Long jobCardId);
}
