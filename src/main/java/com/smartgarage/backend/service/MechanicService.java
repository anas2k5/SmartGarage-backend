package com.smartgarage.backend.service;

import com.smartgarage.backend.model.Mechanic;
import java.util.List;
import java.util.Optional;

public interface MechanicService {

    Mechanic createMechanic(
            Mechanic mechanic,
            Long requesterId,
            String requesterRole
    );

    List<Mechanic> forGarage(Long garageId);

    Mechanic findById(Long id);

    // 🔥 MUST MATCH IMPLEMENTATION
    Optional<Mechanic> findByUserId(Long userId);
}
