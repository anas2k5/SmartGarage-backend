package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.repository.GarageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/garages")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminGarageController {

    private final GarageRepository garageRepository;

    // 🔍 View all garages
    @GetMapping
    public ResponseEntity<List<Garage>> getAllGarages() {
        return ResponseEntity.ok(
                garageRepository.findAll()
        );
    }

    // 🔁 Enable / Disable garage
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Garage> toggleGarage(
            @PathVariable Long id
    ) {
        Garage garage = garageRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Garage not found")
                );

        garage.setActive(!garage.isActive());
        garageRepository.save(garage);

        return ResponseEntity.ok(garage);
    }
}
