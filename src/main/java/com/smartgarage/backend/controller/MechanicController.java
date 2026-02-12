package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Mechanic;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.MechanicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mechanics")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;
    private final UserRepository userRepository;

    // ================= CREATE MECHANIC =================
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<?> create(
            @RequestBody Mechanic mechanic,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        Optional<User> maybeUser =
                userRepository.findByEmail(principal.getName());

        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("Authenticated user not found");
        }

        User actor = maybeUser.get();

        try {
            Mechanic created =
                    mechanicService.createMechanic(
                            mechanic,
                            actor.getId(),
                            actor.getRole()
                    );

            return ResponseEntity.ok(created);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Failed to create mechanic: " + e.getMessage());
        }
    }

    // ================= LIST BY GARAGE =================
    @GetMapping("/garage/{garageId}")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<List<Mechanic>> listForGarage(
            @PathVariable Long garageId
    ) {
        return ResponseEntity.ok(
                mechanicService.forGarage(garageId)
        );
    }

    // ================= GET MY PROFILE =================
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('MECHANIC')")
    public ResponseEntity<?> getMyProfile(Principal principal) {

        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        Optional<User> maybeUser =
                userRepository.findByEmail(principal.getName());

        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = maybeUser.get();

        Optional<Mechanic> mechanic =
                mechanicService.findByUserId(user.getId());

        if (mechanic.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body("Mechanic profile not found");
        }

        return ResponseEntity.ok(mechanic.get());
    }
    // ================= DELETE MECHANIC =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<?> deleteMechanic(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        Optional<User> maybeUser =
                userRepository.findByEmail(principal.getName());

        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        try {
            mechanicService.deleteMechanic(
                    id,
                    maybeUser.get().getId(),
                    maybeUser.get().getRole()
            );

            return ResponseEntity.ok("Mechanic deleted");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Delete failed: " + e.getMessage());
        }
    }
    // ================= UPDATE MECHANIC =================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    public ResponseEntity<?> updateMechanic(
            @PathVariable Long id,
            @RequestBody Mechanic updated,
            Principal principal
    ) {

        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        Optional<User> maybeUser =
                userRepository.findByEmail(principal.getName());

        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        try {
            Mechanic saved =
                    mechanicService.updateMechanic(
                            id,
                            updated,
                            maybeUser.get().getId(),
                            maybeUser.get().getRole()
                    );

            return ResponseEntity.ok(saved);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Update failed: " + e.getMessage());
        }
    }

}
