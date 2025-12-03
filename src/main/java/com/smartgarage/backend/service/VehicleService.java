package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.VehicleRequest;
import com.smartgarage.backend.dto.VehicleResponse;

import java.util.List;

public interface VehicleService {
    VehicleResponse create(Long ownerId, VehicleRequest req);
    List<VehicleResponse> listByOwner(Long ownerId);
    VehicleResponse getById(Long id, Long requesterId); // requesterId used for ownership check
    VehicleResponse update(Long id, Long ownerId, VehicleRequest req);
    void delete(Long id, Long ownerId);
}
