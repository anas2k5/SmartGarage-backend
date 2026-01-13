package com.smartgarage.backend.service;

import com.smartgarage.backend.model.GarageServiceEntity;

import java.util.List;

public interface GarageServiceService {

    List<GarageServiceEntity> getServicesByGarage(Long garageId);
}
