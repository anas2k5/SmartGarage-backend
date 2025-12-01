package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Vehicle;
import com.smartgarage.backend.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService service;
    public VehicleController(VehicleService s){ this.service = s; }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Vehicle> add(@RequestBody Vehicle v){ return ResponseEntity.ok(service.save(v)); }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<Vehicle>> list(@PathVariable Long ownerId){ return ResponseEntity.ok(service.byOwner(ownerId)); }
}
