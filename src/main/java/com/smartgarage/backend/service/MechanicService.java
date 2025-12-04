package com.smartgarage.backend.service;

import com.smartgarage.backend.model.Mechanic;

import java.util.List;

public interface MechanicService {
    Mechanic createMechanic(Mechanic mechanic, Long requesterId, String requesterRole);
    List<Mechanic> forGarage(Long garageId);
    Mechanic findById(Long id);
}
