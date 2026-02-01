package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.repository.GarageRepository;
import com.smartgarage.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/garages")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminGarageController {

    private final GarageRepository garageRepository;
    private final AuditService auditService;

    // ================= GET ALL GARAGES =================
    @GetMapping
    public ResponseEntity<List<Garage>> getAllGarages() {
        return ResponseEntity.ok(garageRepository.findAll());
    }

    // ================= TOGGLE GARAGE =================
    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleGarage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Optional<Garage> optionalGarage = garageRepository.findById(id);

        if (optionalGarage.isEmpty()) {
            return ResponseEntity.badRequest().body("Garage not found");
        }

        Garage garage = optionalGarage.get();
        String oldValue = String.valueOf(garage.isActive());

        garage.setActive(!garage.isActive());
        garageRepository.save(garage);

        // 🔥 AUDIT LOG
        auditService.log(
                null,
                userDetails.getUsername(),
                "ADMIN",
                "GARAGE_TOGGLE",
                "GARAGE",
                garage.getId(),
                oldValue,
                String.valueOf(garage.isActive())
        );

        return ResponseEntity.ok(garage);
    }
}
