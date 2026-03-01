package com.smartgarage.backend.mapper;

import com.smartgarage.backend.dto.GarageRequest;
import com.smartgarage.backend.dto.GarageResponse;
import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.model.User;

public class GarageMapper {

    private GarageMapper() {}

    // ===== ENTITY =====
    public static Garage toEntity(GarageRequest req, User owner) {
        Garage g = new Garage();
        g.setName(req.getName());
        g.setAddress(req.getAddress());
        g.setPhone(req.getPhone());
        g.setOwner(owner);
        g.setActive(true);

        // ✅ ADD COORDS
        g.setLatitude(req.getLatitude());
        g.setLongitude(req.getLongitude());

        return g;
    }

    public static GarageResponse toDto(Garage g) {
        if (g == null) return null;

        Long ownerId = null;
        if (g.getOwner() != null) {
            ownerId = g.getOwner().getId();
        }

        return new GarageResponse(
                g.getId(),
                g.getName(),
                g.getAddress(),
                g.getPhone(),
                g.isActive(),
                ownerId,
                g.getCreatedAt(),
                g.getLatitude(),
                g.getLongitude(),
                g.getMaxBookingsPerSlot()   // ✅ ADD THIS
        );
    }
    // ===== UPDATE =====
    public static void updateFromRequest(Garage g, GarageRequest req) {
        if (req == null || g == null) return;

        if (req.getName() != null) g.setName(req.getName());
        if (req.getAddress() != null) g.setAddress(req.getAddress());
        if (req.getPhone() != null) g.setPhone(req.getPhone());

        // ✅ UPDATE COORDS
        if (req.getLatitude() != null)
            g.setLatitude(req.getLatitude());

        if (req.getLongitude() != null)
            g.setLongitude(req.getLongitude());
    }


}
