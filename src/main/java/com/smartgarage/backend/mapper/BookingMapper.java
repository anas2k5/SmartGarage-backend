package com.smartgarage.backend.mapper;

import com.smartgarage.backend.dto.BookingResponse;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.Mechanic;

public class BookingMapper {

    public static BookingResponse toResponse(Booking b) {
        if (b == null) return null;

        BookingResponse resp = BookingResponse.builder()
                .id(b.getId())
                .garageId(b.getGarage() != null ? b.getGarage().getId() : null)
                .garageName(b.getGarage() != null ? b.getGarage().getName() : null)
                .customerId(b.getCustomer() != null ? b.getCustomer().getId() : null)
                .customerEmail(b.getCustomer() != null ? b.getCustomer().getEmail() : null)
                .vehicleId(b.getVehicle() != null ? b.getVehicle().getId() : null)
                // NOTE: change getPlateNumber() to match your Vehicle entity getter if different
                .vehiclePlate(b.getVehicle() != null ? safeGetVehiclePlate(b.getVehicle()) : null)
                .serviceType(b.getServiceType())
                .bookingTime(b.getBookingTime())
                .status(b.getStatus() != null ? b.getStatus().name() : null)
                .details(b.getDetails())
                .estimatedCost(b.getEstimatedCost())
                .finalCost(b.getFinalCost())
                .build();

        Mechanic m = b.getMechanic();
        if (m != null) {
            resp.setMechanicId(m.getId());
            resp.setMechanicName(m.getName());
            resp.setMechanicPhone(m.getPhone());
        }

        return resp;
    }

    // helper to avoid compile issues — adapt if your Vehicle class uses a different getter
    private static String safeGetVehiclePlate(Object vehicle) {
        try {
            // try common getter names
            try {
                return (String) vehicle.getClass().getMethod("getPlateNumber").invoke(vehicle);
            } catch (NoSuchMethodException ignored) {}
            try {
                return (String) vehicle.getClass().getMethod("getPlate").invoke(vehicle);
            } catch (NoSuchMethodException ignored) {}
            try {
                return (String) vehicle.getClass().getMethod("getPlateNo").invoke(vehicle);
            } catch (NoSuchMethodException ignored) {}
            // fallback to toString
            return vehicle.toString();
        } catch (Exception ex) {
            return null;
        }
    }
}
