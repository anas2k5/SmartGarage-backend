package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.repository.GarageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/garages")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminGarageController {

    private final GarageRepository garageRepository;

    // 🔍 View all garages
    @GetMapping
    public ResponseEntity<List<Garage>> getAllGarages() {
        return ResponseEntity.ok(garageRepository.findAll());
    }

    // 🔁 Enable / Disable garage safely
    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleGarage(@PathVariable Long id) {

        Optional<Garage> optionalGarage = garageRepository.findById(id);

        if (optionalGarage.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Garage not found");
        }

        Garage garage = optionalGarage.get();
        garage.setActive(!garage.isActive());
        garageRepository.save(garage);

        return ResponseEntity.ok(garage);
    }
}
