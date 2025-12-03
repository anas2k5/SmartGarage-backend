package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByOwnerId(Long ownerId);
    Optional<Vehicle> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<Vehicle> findByPlateNumber(String plateNumber);
}
