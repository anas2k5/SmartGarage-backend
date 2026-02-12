package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.JobCardService;
import com.smartgarage.backend.service.NotificationService;
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
    private final JobCardTaskRepository taskRepository;
    private final JobCardPartRepository partRepository;
    private final NotificationService notificationService;


    // ================= FETCH BY MECHANIC =================
    @Override
    public List<JobCard> getByMechanic(Long mechanicId) {
        return jobCardRepository.findByMechanicId(mechanicId);
    }

    // ================= FETCH BY GARAGE (OWNER) =================
    @Override
    public List<JobCard> getByGarage(Long garageId) {
        return jobCardRepository.findByGarageId(garageId);
    }

    // ================= ADD TASK =================
    @Override
    public JobCardTask addTask(
            Long jobCardId,
            String description,
            Double hours,
            Double cost
    ) {
        JobCard card = jobCardRepository.findById(jobCardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("JobCard not found"));

        if (card.getStatus() == JobCardStatus.CLOSED) {
            throw new IllegalStateException("Job already closed");
        }

        JobCardTask task = JobCardTask.builder()
                .jobCard(card)
                .description(description)
                .hours(hours)
                .cost(cost)
                .build();

        return taskRepository.save(task);
    }

    // ================= ADD PART =================
    @Override
    public JobCardPart addPart(
            Long jobCardId,
            String name,
            Integer quantity,
            Double unitPrice
    ) {
        JobCard card = jobCardRepository.findById(jobCardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("JobCard not found"));

        if (card.getStatus() == JobCardStatus.CLOSED) {
            throw new IllegalStateException("Job already closed");
        }

        JobCardPart part = JobCardPart.builder()
                .jobCard(card)
                .name(name)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();

        return partRepository.save(part);
    }

    // ================= START WORK =================
    @Override
    public JobCard approveJob(Long jobCardId) {

        JobCard card = jobCardRepository.findById(jobCardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("JobCard not found"));

        card.setStatus(JobCardStatus.WORKING);

        // Sync booking
        Booking booking = card.getBooking();
        booking.setStatus(BookingStatus.IN_PROGRESS);

        bookingRepository.save(booking);
        notificationService.create(
                booking.getCustomer().getId(),
                "Service Started",
                "Work on your vehicle has begun.",
                "JOB_CARD"
        );

        return jobCardRepository.save(card);
    }

    @Override
    public JobCard closeJob(Long jobCardId) {

        JobCard card = jobCardRepository.findById(jobCardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("JobCard not found"));

        if (card.getStatus() == JobCardStatus.CLOSED) {
            throw new IllegalStateException("Job already closed");
        }

        // 1️⃣ Close job card
        card.setStatus(JobCardStatus.CLOSED);
        card.setClosedAt(LocalDateTime.now());

        // 2️⃣ Fetch tasks + parts
        List<JobCardTask> tasks =
                taskRepository.findByJobCardId(jobCardId);

        List<JobCardPart> parts =
                partRepository.findByJobCardId(jobCardId);

        // 3️⃣ Calculate labor cost
        double laborCost = tasks.stream()
                .mapToDouble(t ->
                        t.getCost() != null ? t.getCost() : 0)
                .sum();

        // 4️⃣ Calculate parts cost
        double partsCost = parts.stream()
                .mapToDouble(p ->
                        (p.getQuantity() != null ? p.getQuantity() : 0) *
                                (p.getUnitPrice() != null ? p.getUnitPrice() : 0))
                .sum();

        double totalCost = laborCost + partsCost;
        System.out.println("🔥 CLOSE JOB EXECUTED");
        System.out.println("Total cost = " + totalCost);

        // 5️⃣ Save cost in job card
        card.setLaborCost(laborCost);
        card.setPartsCost(partsCost);
        card.setTotalCost(totalCost);   // ⭐ IMPORTANT

        // 6️⃣ Sync booking (Owner finalizes later)
        Booking booking = card.getBooking();
        booking.setFinalCost(totalCost);

        bookingRepository.saveAndFlush(booking);
        notificationService.create(
                booking.getCustomer().getId(),
                "Service Completed",
                "Your vehicle service has been completed. Please review and pay.",
                "JOB_CARD"
        );
        return jobCardRepository.saveAndFlush(card);
    }

}
