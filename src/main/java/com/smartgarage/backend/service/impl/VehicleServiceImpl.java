package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.VehicleRequest;
import com.smartgarage.backend.dto.VehicleResponse;
import com.smartgarage.backend.mapper.VehicleMapper;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.model.Vehicle;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.repository.VehicleRepository;
import com.smartgarage.backend.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VehicleResponse create(Long ownerId, VehicleRequest req) {
        // validate owner exists
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        // plate uniqueness check (global). If you prefer uniqueness per owner use repository method accordingly
        vehicleRepository.findByPlateNumber(req.getPlateNumber()).ifPresent(v -> {
            throw new IllegalArgumentException("Vehicle with this plate number already exists");
        });

        Vehicle v = VehicleMapper.toEntity(req);
        v.setOwner(owner);
        Vehicle saved = vehicleRepository.save(v);
        return VehicleMapper.toResponse(saved);
    }

    @Override
    public List<VehicleResponse> listByOwner(Long ownerId) {
        List<Vehicle> list = vehicleRepository.findByOwnerId(ownerId);
        return list.stream().map(VehicleMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getById(Long id, Long requesterId) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        if (v.getOwner() == null || !v.getOwner().getId().equals(requesterId)) {
            throw new SecurityException("Not authorized to view this vehicle");
        }
        return VehicleMapper.toResponse(v);
    }

    @Override
    public VehicleResponse update(Long id, Long ownerId, VehicleRequest req) {
        Vehicle v = vehicleRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found or you are not the owner"));

        // If plate number updated, ensure uniqueness
        if (req.getPlateNumber() != null && !req.getPlateNumber().equals(v.getPlateNumber())) {
            vehicleRepository.findByPlateNumber(req.getPlateNumber()).ifPresent(existing -> {
                throw new IllegalArgumentException("Another vehicle with this plate number already exists");
            });
        }

        VehicleMapper.updateEntity(v, req);
        Vehicle saved = vehicleRepository.save(v);
        return VehicleMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id, Long ownerId) {
        Vehicle v = vehicleRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found or you are not the owner"));
        vehicleRepository.delete(v);
    }
}
