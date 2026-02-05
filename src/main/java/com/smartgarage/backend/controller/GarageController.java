package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.GarageRequest;
import com.smartgarage.backend.dto.GarageResponse;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.GarageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/garages")
@RequiredArgsConstructor
public class GarageController {

    private final GarageService garageService;
    private final UserRepository userRepository;

    // ================= OWNER: MY GARAGES =================
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<List<GarageResponse>> myGarages(
            Principal principal
    ) {
        User owner = userRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                garageService.listByOwner(owner.getId())
        );
    }

    // ================= CUSTOMER: BROWSE =================
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER','ADMIN')")
    public ResponseEntity<List<GarageResponse>> getActiveGarages() {
        return ResponseEntity.ok(
                garageService.listAll()
        );
    }

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<GarageResponse> create(
            @RequestBody GarageRequest req,
            Principal principal
    ) {
        User owner = userRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                garageService.create(owner.getId(), req)
        );
    }

    // ================= UPDATE (EDIT) =================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<GarageResponse> update(
            @PathVariable Long id,
            @RequestBody GarageRequest req,
            Principal principal
    ) {
        User owner = userRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                garageService.update(id, owner.getId(), req)
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            Principal principal
    ) {
        User owner = userRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        garageService.delete(id, owner.getId());

        return ResponseEntity.ok("Garage deleted successfully");
    }
}
