package com.smartgarage.backend.mapper;

import com.smartgarage.backend.dto.VehicleRequest;
import com.smartgarage.backend.dto.VehicleResponse;
import com.smartgarage.backend.model.Vehicle;

import java.time.LocalDateTime;

public class VehicleMapper {

    public static com.smartgarage.backend.model.Vehicle toEntity(VehicleRequest req) {
        Vehicle v = new Vehicle();
        v.setPlateNumber(req.getPlateNumber());
        v.setMake(req.getMake());
        v.setModel(req.getModel());
        return v;
    }

    public static VehicleResponse toResponse(Vehicle v) {
        Long ownerId = v.getOwner() != null ? v.getOwner().getId() : null;

        return new VehicleResponse(
                v.getId(),
                v.getPlateNumber(),
                v.getMake(),
                v.getModel(),
                ownerId,
                null  // createdAt (optional)
        );
    }

    public static void updateEntity(Vehicle v, VehicleRequest req) {
        if (req.getPlateNumber() != null) v.setPlateNumber(req.getPlateNumber());
        if (req.getMake() != null) v.setMake(req.getMake());
        if (req.getModel() != null) v.setModel(req.getModel());
    }
}
