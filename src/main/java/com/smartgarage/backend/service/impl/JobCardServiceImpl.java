package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.JobCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobCardServiceImpl implements JobCardService {

    private final JobCardRepository jobCardRepository;
    private final BookingRepository bookingRepository;
    private final MechanicRepository mechanicRepository;
    private final JobCardTaskRepository taskRepository;
    private final JobCardPartRepository partRepository;

    // ================= CREATE =================
    @Override
    public JobCard createJobCard(
            Long bookingId,
            Long mechanicId,
            String requesterEmail
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found"));

        JobCard jobCard = JobCard.builder()
                .booking(booking)
                .mechanic(mechanic)
                .status(JobCardStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        return jobCardRepository.save(jobCard);
    }

    // ================= FETCH =================
    @Override
    public List<JobCard> getByGarage(Long garageId) {
        return jobCardRepository.findByBookingGarageId(garageId);
    }

    @Override
    public List<JobCard> getByMechanic(Long mechanicId) {
        return jobCardRepository.findByMechanicId(mechanicId);
    }

    // ================= TASK =================
    @Override
    public JobCardTask addTask(
            Long jobCardId,
            String description,
            Double hours,
            Double cost
    ) {
        JobCard card = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("JobCard not found"));

        JobCardTask task = JobCardTask.builder()
                .jobCard(card)
                .description(description)
                .hours(hours)
                .cost(cost)
                .build();

        return taskRepository.save(task);
    }

    // ================= PART =================
    @Override
    public JobCardPart addPart(
            Long jobCardId,
            String name,
            Integer quantity,
            Double unitPrice
    ) {
        JobCard card = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("JobCard not found"));

        JobCardPart part = JobCardPart.builder()
                .jobCard(card)
                .name(name)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();

        return partRepository.save(part);
    }

    // ================= APPROVE =================
    @Override
    public JobCard approveJob(Long jobCardId) {
        JobCard card = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("JobCard not found"));

        card.setStatus(JobCardStatus.WORKING);
        return jobCardRepository.save(card);
    }

    // ================= CLOSE =================
    @Override
    public JobCard closeJob(Long jobCardId) {
        JobCard card = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("JobCard not found"));

        card.setStatus(JobCardStatus.CLOSED);
        card.setClosedAt(LocalDateTime.now());
        return jobCardRepository.save(card);
    }
}
