package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Garage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Long> {
    List<Garage> findByOwnerId(Long ownerId);
}
