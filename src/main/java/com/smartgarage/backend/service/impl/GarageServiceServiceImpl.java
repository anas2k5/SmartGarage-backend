package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.model.GarageServiceEntity;
import com.smartgarage.backend.repository.GarageServiceRepository;
import com.smartgarage.backend.service.GarageServiceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GarageServiceServiceImpl implements GarageServiceService {

    private final GarageServiceRepository repo;

    public GarageServiceServiceImpl(GarageServiceRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<GarageServiceEntity> getServicesByGarage(Long garageId) {
        return repo.findByGarageIdAndActiveTrue(garageId);
    }
}
