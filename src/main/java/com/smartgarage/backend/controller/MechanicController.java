package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Mechanic;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.MechanicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mechanics")
public class MechanicController {

    private final MechanicService mechanicService;
    private final UserRepository userRepository;

    public MechanicController(MechanicService mechanicService, UserRepository userRepository) {
        this.mechanicService = mechanicService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Mechanic mechanic, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        Optional<User> maybe = userRepository.findByEmail(principal.getName());
        if (maybe.isEmpty()) return ResponseEntity.status(401).body("Authenticated user not found");
        User actor = maybe.get();

        try {
            Mechanic created = mechanicService.createMechanic(mechanic, actor.getId(), actor.getRole());
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to create mechanic: " + ex.getMessage());
        }
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<?> listForGarage(@PathVariable("garageId") Long garageId) {
        List<Mechanic> list = mechanicService.forGarage(garageId);
        return ResponseEntity.ok(list);
    }
}
