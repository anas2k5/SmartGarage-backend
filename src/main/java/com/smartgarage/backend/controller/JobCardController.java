package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.*;
import com.smartgarage.backend.service.AuditService;
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
    private final AuditService auditService;

    // ================= CREATE JOB CARD =================
    @PostMapping("/booking/{bookingId}/mechanic/{mechanicId}")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<JobCard> createJobCard(
            @PathVariable Long bookingId,
            @PathVariable Long mechanicId,
            @AuthenticationPrincipal UserDetails user
    ) {
        JobCard jobCard =
                jobCardService.createJobCard(bookingId, mechanicId, user.getUsername());

        // 🔥 AUDIT
        auditService.log(
                AuditModule.JOB_CARD_MANAGEMENT,
                null,
                user.getUsername(),
                "OWNER",
                "JOB_CARD_CREATED",
                "JOB_CARD",
                jobCard.getId(),
                "NONE",
                "OPEN"
        );

        return ResponseEntity.ok(jobCard);
    }

    // ================= GET BY GARAGE =================
    @GetMapping("/garage/{garageId}")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<List<JobCard>> getByGarage(
            @PathVariable Long garageId
    ) {
        return ResponseEntity.ok(
                jobCardService.getByGarage(garageId)
        );
    }

    // ================= GET BY MECHANIC =================
    @GetMapping("/mechanic/{mechanicId}")
    @PreAuthorize("hasAnyAuthority('MECHANIC','ADMIN')")
    public ResponseEntity<List<JobCard>> getByMechanic(
            @PathVariable Long mechanicId
    ) {
        return ResponseEntity.ok(
                jobCardService.getByMechanic(mechanicId)
        );
    }

    // ================= ADD TASK =================
    @PostMapping("/{jobCardId}/tasks")
    @PreAuthorize("hasAnyAuthority('MECHANIC','OWNER')")
    public ResponseEntity<JobCardTask> addTask(
            @PathVariable Long jobCardId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails user
    ) {
        String description = body.get("description").toString();
        Double hours = Double.valueOf(body.get("hours").toString());
        Double cost = Double.valueOf(body.get("cost").toString());

        JobCardTask task =
                jobCardService.addTask(jobCardId, description, hours, cost);

        auditService.log(
                AuditModule.JOB_CARD_MANAGEMENT,
                null,
                user.getUsername(),
                "MECHANIC",
                "TASK_ADDED",
                "JOB_CARD",
                jobCardId,
                "NONE",
                description
        );

        return ResponseEntity.ok(task);
    }

    // ================= ADD PART =================
    @PostMapping("/{jobCardId}/parts")
    @PreAuthorize("hasAnyAuthority('MECHANIC','OWNER')")
    public ResponseEntity<JobCardPart> addPart(
            @PathVariable Long jobCardId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails user
    ) {
        String name = body.get("name").toString();
        Integer qty = Integer.parseInt(body.get("quantity").toString());
        Double price = Double.valueOf(body.get("unitPrice").toString());

        JobCardPart part =
                jobCardService.addPart(jobCardId, name, qty, price);

        auditService.log(
                AuditModule.JOB_CARD_MANAGEMENT,
                null,
                user.getUsername(),
                "MECHANIC",
                "PART_ADDED",
                "JOB_CARD",
                jobCardId,
                "NONE",
                name
        );

        return ResponseEntity.ok(part);
    }

    // ================= APPROVE JOB =================
    @PutMapping("/{jobCardId}/approve")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<JobCard> approve(
            @PathVariable Long jobCardId,
            @AuthenticationPrincipal UserDetails user
    ) {
        JobCard card = jobCardService.approveJob(jobCardId);

        auditService.log(
                AuditModule.JOB_CARD_MANAGEMENT,
                null,
                user.getUsername(),
                "OWNER",
                "JOB_APPROVED",
                "JOB_CARD",
                jobCardId,
                "WAITING_APPROVAL",
                "WORKING"
        );

        return ResponseEntity.ok(card);
    }

    // ================= CLOSE JOB =================
    @PutMapping("/{jobCardId}/close")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<JobCard> close(
            @PathVariable Long jobCardId,
            @AuthenticationPrincipal UserDetails user
    ) {
        JobCard card = jobCardService.closeJob(jobCardId);

        auditService.log(
                AuditModule.JOB_CARD_MANAGEMENT,
                null,
                user.getUsername(),
                "OWNER",
                "JOB_CLOSED",
                "JOB_CARD",
                jobCardId,
                "WORKING",
                "CLOSED"
        );

        return ResponseEntity.ok(card);
    }
}
