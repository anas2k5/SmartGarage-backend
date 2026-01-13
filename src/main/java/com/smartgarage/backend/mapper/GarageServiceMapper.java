package com.smartgarage.backend.mapper;

import com.smartgarage.backend.dto.GarageServiceResponse;
import com.smartgarage.backend.model.GarageServiceEntity;

public class GarageServiceMapper {

    public static GarageServiceResponse toResponse(GarageServiceEntity e) {
        return GarageServiceResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .build();
    }
}
