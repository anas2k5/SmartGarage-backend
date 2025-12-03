package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.VehicleRequest;
import com.smartgarage.backend.dto.VehicleResponse;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserRepository userRepository;

    public VehicleController(VehicleService vehicleService, UserRepository userRepository) {
        this.vehicleService = vehicleService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody VehicleRequest req, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");

        User user = maybe.get();
        try {
            VehicleResponse resp = vehicleService.create(user.getId(), req);
            URI loc = URI.create("/api/vehicles/" + resp.getId());
            return ResponseEntity.created(loc).body(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to create vehicle: " + ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listMyVehicles(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User user = maybe.get();

        List<VehicleResponse> list = vehicleService.listByOwner(user.getId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id, Principal principal) {
        if (principal == null || principal.getName() == null) return ResponseEntity.status(401).body("Unauthenticated");
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User user = maybe.get();

        try {
            VehicleResponse resp = vehicleService.getById(id, user.getId());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @Valid @RequestBody VehicleRequest req, Principal principal) {
        if (principal == null || principal.getName() == null) return ResponseEntity.status(401).body("Unauthenticated");
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User user = maybe.get();

        try {
            VehicleResponse resp = vehicleService.update(id, user.getId(), req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update vehicle: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id, Principal principal) {
        if (principal == null || principal.getName() == null) return ResponseEntity.status(401).body("Unauthenticated");
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User user = maybe.get();

        try {
            vehicleService.delete(id, user.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to delete vehicle: " + ex.getMessage());
        }
    }
}
