package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.GarageServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarageServiceRepository
        extends JpaRepository<GarageServiceEntity, Long> {

    List<GarageServiceEntity> findByGarageIdAndActiveTrue(Long garageId);
}
