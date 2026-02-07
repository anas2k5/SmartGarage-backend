package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.MechanicRepository;
import com.smartgarage.backend.service.JobCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobcards")
@RequiredArgsConstructor
public class JobCardController {

    private final JobCardService jobCardService;
    private final MechanicRepository mechanicRepository;

    // =========================================================
    // OWNER → GET GARAGE JOB CARDS
    // =========================================================
    @GetMapping("/garage/{garageId}")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<List<JobCard>> getGarageJobs(
            @PathVariable Long garageId
    ) {
        return ResponseEntity.ok(
                jobCardService.getByGarage(garageId)
        );
    }

    // =========================================================
    // MECHANIC → MY JOBS
    // =========================================================
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('MECHANIC','ADMIN')")
    public ResponseEntity<List<JobCard>> getMyJobs(
            @AuthenticationPrincipal UserDetails user
    ) {

        Mechanic mechanic = mechanicRepository
                .findByUserEmail(user.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Mechanic profile not linked")
                );

        return ResponseEntity.ok(
                jobCardService.getByMechanic(mechanic.getId())
        );
    }

    // =========================================================
    // ADD TASK
    // =========================================================
    @PostMapping("/{jobCardId}/tasks")
    @PreAuthorize("hasAuthority('MECHANIC')")
    public ResponseEntity<JobCardTask> addTask(
            @PathVariable Long jobCardId,
            @RequestBody Map<String, Object> body
    ) {

        String desc = body.get("description").toString();
        Double hours = Double.valueOf(body.get("hours").toString());
        Double cost = Double.valueOf(body.get("cost").toString());

        return ResponseEntity.ok(
                jobCardService.addTask(jobCardId, desc, hours, cost)
        );
    }

    // =========================================================
    // ADD PART
    // =========================================================
    @PostMapping("/{jobCardId}/parts")
    @PreAuthorize("hasAuthority('MECHANIC')")
    public ResponseEntity<JobCardPart> addPart(
            @PathVariable Long jobCardId,
            @RequestBody Map<String, Object> body
    ) {

        String name = body.get("name").toString();
        Integer qty = Integer.parseInt(body.get("quantity").toString());
        Double price = Double.valueOf(body.get("unitPrice").toString());

        return ResponseEntity.ok(
                jobCardService.addPart(jobCardId, name, qty, price)
        );
    }

    // =========================================================
    // START WORK
    // =========================================================
    @PutMapping("/{jobCardId}/approve")
    @PreAuthorize("hasAuthority('MECHANIC')")
    public ResponseEntity<JobCard> startWork(
            @PathVariable Long jobCardId
    ) {
        return ResponseEntity.ok(
                jobCardService.approveJob(jobCardId)
        );
    }

    // =========================================================
    // CLOSE JOB
    // =========================================================
    @PutMapping("/{jobCardId}/close")
    @PreAuthorize("hasAuthority('MECHANIC')")
    public ResponseEntity<JobCard> closeJob(
            @PathVariable Long jobCardId
    ) {
        return ResponseEntity.ok(
                jobCardService.closeJob(jobCardId)
        );
    }
}
