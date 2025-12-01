package com.smartgarage.backend.mapper;


import com.smartgarage.backend.dto.BookingResponse;
import com.smartgarage.backend.model.Booking;

public class BookingMapper {

    public static BookingResponse toResponse(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getGarage().getId(),
                b.getGarage().getName(),
                b.getCustomer().getId(),
                b.getCustomer().getEmail(),
                b.getVehicle().getId(),
                b.getServiceType(),
                b.getBookingTime(),
                b.getStatus().toString(),
                b.getDetails()
        );
    }
}
