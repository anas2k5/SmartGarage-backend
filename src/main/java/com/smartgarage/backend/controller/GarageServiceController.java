package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.GarageServiceResponse;
import com.smartgarage.backend.mapper.GarageServiceMapper;
import com.smartgarage.backend.repository.GarageServiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/garages")
public class GarageServiceController {

    private final GarageServiceRepository repo;

    public GarageServiceController(GarageServiceRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{garageId}/services")
    public ResponseEntity<List<GarageServiceResponse>> getGarageServices(
            @PathVariable Long garageId
    ) {
        return ResponseEntity.ok(
                repo.findByGarageIdAndActiveTrue(garageId)
                        .stream()
                        .map(GarageServiceMapper::toResponse)
                        .toList()
        );
    }
}
