package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.VehicleRequest;
import com.smartgarage.backend.dto.VehicleResponse;
import com.smartgarage.backend.security.CustomUserDetails;
import com.smartgarage.backend.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) return null;
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody VehicleRequest req) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        VehicleResponse resp = vehicleService.create(userId, req);
        URI loc = URI.create("/api/vehicles/" + resp.getId());
        return ResponseEntity.created(loc).body(resp);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> listMyVehicles() {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(vehicleService.listByOwner(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        VehicleResponse resp = vehicleService.getById(id, userId);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody VehicleRequest req) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        VehicleResponse resp = vehicleService.update(id, userId, req);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        vehicleService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
