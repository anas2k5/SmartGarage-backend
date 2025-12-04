package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.exception.ForbiddenException;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.model.Mechanic;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.GarageRepository;
import com.smartgarage.backend.repository.MechanicRepository;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.MechanicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MechanicServiceImpl implements MechanicService {

    private final MechanicRepository mechanicRepository;
    private final GarageRepository garageRepository;
    private final UserRepository userRepository;

    public MechanicServiceImpl(MechanicRepository mechanicRepository,
                               GarageRepository garageRepository,
                               UserRepository userRepository) {
        this.mechanicRepository = mechanicRepository;
        this.garageRepository = garageRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Mechanic createMechanic(Mechanic mechanic, Long requesterId, String requesterRole) {
        if (mechanic == null || mechanic.getGarage() == null || mechanic.getGarage().getId() == null) {
            throw new IllegalArgumentException("Mechanic and garageId required");
        }

        Garage g = garageRepository.findById(mechanic.getGarage().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        Long ownerId = g.getOwner() != null ? g.getOwner().getId() : null;
        boolean isOwner = ownerId != null && ownerId.equals(requesterId);
        boolean isAdmin = requesterRole != null && requesterRole.equalsIgnoreCase("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Only the garage owner or admin can add mechanics");
        }

        mechanic.setGarage(g);
        return mechanicRepository.save(mechanic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mechanic> forGarage(Long garageId) {
        return mechanicRepository.findByGarageId(garageId);
    }

    @Override
    public Mechanic findById(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found"));
    }
}
