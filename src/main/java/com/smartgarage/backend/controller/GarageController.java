package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.GarageRepository;
import com.smartgarage.backend.repository.UserRepository;
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

    private final GarageRepository garageRepository;
    private final UserRepository userRepository;

    // ================= OWNER: MY GARAGES =================
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<List<Garage>> myGarages(
            Principal principal
    ) {
        User owner = userRepository
                .findByEmail(principal.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return ResponseEntity.ok(
                garageRepository.findByOwnerId(owner.getId())
        );
    }
}
