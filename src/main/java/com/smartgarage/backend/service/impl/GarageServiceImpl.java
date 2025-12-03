package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.GarageRequest;
import com.smartgarage.backend.dto.GarageResponse;
import com.smartgarage.backend.exception.ForbiddenException;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.mapper.GarageMapper;
import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.GarageRepository;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.GarageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;
    private final UserRepository userRepository;

    public GarageServiceImpl(GarageRepository garageRepository, UserRepository userRepository) {
        this.garageRepository = garageRepository;
        this.userRepository = userRepository;
    }

    @Override
    public GarageResponse create(Long ownerId, GarageRequest req) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        // optionally validate owner role is OWNER (or admin)
        Garage g = GarageMapper.toEntity(req, owner);
        Garage saved = garageRepository.save(g);
        return GarageMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GarageResponse> listAll() {
        return garageRepository.findAll().stream().map(GarageMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GarageResponse> listByOwner(Long ownerId) {
        return garageRepository.findByOwnerId(ownerId).stream().map(GarageMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GarageResponse getById(Long id) {
        Garage g = garageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Garage not found"));
        return GarageMapper.toDto(g);
    }

    @Override
    public GarageResponse update(Long id, Long ownerId, GarageRequest req) {
        Garage g = garageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Garage not found"));
        if (!g.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("You do not own this garage");
        }
        GarageMapper.updateFromRequest(g, req);
        Garage saved = garageRepository.save(g);
        return GarageMapper.toDto(saved);
    }

    @Override
    public void delete(Long id, Long ownerId) {
        Garage g = garageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Garage not found"));
        if (!g.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("You do not own this garage");
        }
        garageRepository.delete(g);
    }
}
