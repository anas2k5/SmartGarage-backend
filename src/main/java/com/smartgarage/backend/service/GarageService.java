package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.GarageRequest;
import com.smartgarage.backend.dto.GarageResponse;

import java.util.List;

public interface GarageService {
    GarageResponse create(Long ownerId, GarageRequest req);
    List<GarageResponse> listAll();
    List<GarageResponse> listByOwner(Long ownerId);
    GarageResponse getById(Long id);
    GarageResponse update(Long id, Long ownerId, GarageRequest req);
    void delete(Long id, Long ownerId);
}
