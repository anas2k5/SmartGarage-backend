package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Garage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GarageRepository extends JpaRepository<Garage, Long> {
}
