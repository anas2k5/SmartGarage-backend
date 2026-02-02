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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MechanicServiceImpl implements MechanicService {

    private final MechanicRepository mechanicRepository;
    private final GarageRepository garageRepository;
    private final UserRepository userRepository;

    // ================= CREATE MECHANIC =================
    @Override
    public Mechanic createMechanic(
            Mechanic mechanic,
            Long requesterId,
            String requesterRole
    ) {
        if (mechanic == null ||
                mechanic.getGarage() == null ||
                mechanic.getGarage().getId() == null ||
                mechanic.getUser() == null ||
                mechanic.getUser().getId() == null
        ) {
            throw new IllegalArgumentException("Mechanic, userId and garageId are required");
        }

        // ----------------------------
        // LOAD GARAGE
        // ----------------------------
        Garage garage = garageRepository.findById(
                mechanic.getGarage().getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Garage not found")
        );

        // ----------------------------
        // LOAD USER
        // ----------------------------
        User user = userRepository.findById(
                mechanic.getUser().getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("User not found")
        );

        // ----------------------------
        // SECURITY CHECK
        // ----------------------------
        Long ownerId = garage.getOwner() != null
                ? garage.getOwner().getId()
                : null;

        boolean isOwner =
                ownerId != null && ownerId.equals(requesterId);

        boolean isAdmin =
                requesterRole != null &&
                        requesterRole.equalsIgnoreCase("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(
                    "Only the garage owner or admin can add mechanics"
            );
        }

        // ----------------------------
        // DUPLICATE PREVENTION
        // ----------------------------
        mechanicRepository.findByUserId(user.getId())
                .ifPresent(m -> {
                    throw new IllegalStateException(
                            "This user is already registered as a mechanic"
                    );
                });

        // ----------------------------
        // ROLE ENFORCEMENT
        // ----------------------------
        if (!"MECHANIC".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException(
                    "User role must be MECHANIC to be linked"
            );
        }

        // ----------------------------
        // LINK ENTITIES
        // ----------------------------
        mechanic.setGarage(garage);
        mechanic.setUser(user);

        return mechanicRepository.save(mechanic);
    }

    // ================= FIND BY GARAGE =================
    @Override
    @Transactional(readOnly = true)
    public List<Mechanic> forGarage(Long garageId) {
        return mechanicRepository.findByGarageId(garageId);
    }

    // ================= FIND BY ID =================
    @Override
    @Transactional(readOnly = true)
    public Mechanic findById(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Mechanic not found")
                );
    }

    // ================= FIND BY USER ID =================
    @Override
    @Transactional(readOnly = true)
    public Optional<Mechanic> findByUserId(Long userId) {
        return mechanicRepository.findByUserId(userId);
    }
}
