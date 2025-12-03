package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.GarageRequest;
import com.smartgarage.backend.dto.GarageResponse;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.GarageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/garages")
public class GarageController {

    private final GarageService garageService;
    private final UserRepository userRepository;

    public GarageController(GarageService garageService, UserRepository userRepository) {
        this.garageService = garageService;
        this.userRepository = userRepository;
    }

    // Public listing
    @GetMapping
    public ResponseEntity<List<GarageResponse>> listAll() {
        return ResponseEntity.ok(garageService.listAll());
    }

    // Get one (public)
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(garageService.getById(id));
    }

    // Create — only OWNER or ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody GarageRequest req, Principal principal) {
        if (principal == null || principal.getName() == null) return ResponseEntity.status(401).body("Unauthenticated");
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User user = maybe.get();

        GarageResponse resp = garageService.create(user.getId(), req);
        URI loc = URI.create("/api/garages/" + resp.getId());
        return ResponseEntity.created(loc).body(resp);
    }

    // Owner update
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody GarageRequest req, Principal principal) {
        if (principal == null || principal.getName() == null) return ResponseEntity.status(401).body("Unauthenticated");
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User user = maybe.get();

        GarageResponse updated = garageService.update(id, user.getId(), req);
        return ResponseEntity.ok(updated);
    }

    // Owner delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        if (principal == null || principal.getName() == null) return ResponseEntity.status(401).body("Unauthenticated");
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User user = maybe.get();

        garageService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // Owner's garages
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<?> myGarages(Principal principal) {
        if (principal == null || principal.getName() == null) return ResponseEntity.status(401).body("Unauthenticated");
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User user = maybe.get();

        return ResponseEntity.ok(garageService.listByOwner(user.getId()));
    }
}
