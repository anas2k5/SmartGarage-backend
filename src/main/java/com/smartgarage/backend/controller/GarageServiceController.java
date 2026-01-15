package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.GarageServiceRequest;
import com.smartgarage.backend.dto.GarageServiceResponse;
import com.smartgarage.backend.mapper.GarageServiceMapper;
import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.model.GarageServiceEntity;
import com.smartgarage.backend.repository.GarageRepository;
import com.smartgarage.backend.repository.GarageServiceRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/garages")
public class GarageServiceController {

    private final GarageServiceRepository serviceRepo;
    private final GarageRepository garageRepo;

    public GarageServiceController(
            GarageServiceRepository serviceRepo,
            GarageRepository garageRepo
    ) {
        this.serviceRepo = serviceRepo;
        this.garageRepo = garageRepo;
    }

    // ================= GET ACTIVE SERVICES =================
    @GetMapping("/{garageId}/services")
    public ResponseEntity<List<GarageServiceResponse>> getGarageServices(
            @PathVariable Long garageId
    ) {
        return ResponseEntity.ok(
                serviceRepo.findByGarageIdAndActiveTrue(garageId)
                        .stream()
                        .map(GarageServiceMapper::toResponse)
                        .toList()
        );
    }

    // ================= ADD SERVICE =================
    @PostMapping("/{garageId}/services")
    public ResponseEntity<GarageServiceResponse> addService(
            @PathVariable Long garageId,
            @Valid @RequestBody GarageServiceRequest request
    ) {
        Garage garage = garageRepo.findById(garageId)
                .orElseThrow(() -> new RuntimeException("Garage not found"));

        GarageServiceEntity service = new GarageServiceEntity();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setActive(true);
        service.setGarage(garage);

        return ResponseEntity.ok(
                GarageServiceMapper.toResponse(serviceRepo.save(service))
        );
    }

    // ================= UPDATE SERVICE (EDIT) =================
    @PutMapping("/services/{serviceId}")
    public ResponseEntity<GarageServiceResponse> updateService(
            @PathVariable Long serviceId,
            @Valid @RequestBody GarageServiceRequest request
    ) {
        GarageServiceEntity service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());

        return ResponseEntity.ok(
                GarageServiceMapper.toResponse(serviceRepo.save(service))
        );
    }

    // ================= DEACTIVATE SERVICE (SOFT DELETE) =================
    @PutMapping("/services/{serviceId}/deactivate")
    public ResponseEntity<?> deactivateService(@PathVariable Long serviceId) {
        GarageServiceEntity service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        service.setActive(false);
        serviceRepo.save(service);

        return ResponseEntity.ok().build();
    }
}
