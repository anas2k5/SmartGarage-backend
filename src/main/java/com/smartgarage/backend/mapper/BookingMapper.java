package com.smartgarage.backend.mapper;

import com.smartgarage.backend.dto.BookingResponse;
import com.smartgarage.backend.model.Booking;

public class BookingMapper {

    private BookingMapper() {}

    public static BookingResponse toResponse(Booking b) {
        if (b == null) return null;
        Long garageId = b.getGarage() != null ? b.getGarage().getId() : null;
        String garageName = b.getGarage() != null ? b.getGarage().getName() : null;
        Long customerId = b.getCustomer() != null ? b.getCustomer().getId() : null;
        String customerEmail = b.getCustomer() != null ? b.getCustomer().getEmail() : null;
        Long vehicleId = b.getVehicle() != null ? b.getVehicle().getId() : null;
        String vehiclePlate = b.getVehicle() != null ? b.getVehicle().getPlateNumber() : null;

        return new BookingResponse(
                b.getId(),
                garageId,
                garageName,
                customerId,
                customerEmail,
                vehicleId,
                vehiclePlate,
                b.getServiceType(),
                b.getBookingTime(),
                b.getStatus(),
                b.getDetails()
        );
    }
}
