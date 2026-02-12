package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MechanicRepository extends JpaRepository<Mechanic, Long> {

    List<Mechanic> findByGarageId(Long garageId);

    // 🔥 FIND MECHANIC BY LOGIN EMAIL
    Optional<Mechanic> findByUserEmail(String email);
    Optional<Mechanic> findByUserId(Long userId);

    long countByGarageOwnerId(Long ownerId);
}
