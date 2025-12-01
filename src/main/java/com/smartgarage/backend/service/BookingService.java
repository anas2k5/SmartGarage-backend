package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.model.Booking;

import java.util.Optional;
import java.util.List;

public interface BookingService {
    /**
     * Save a booking entity (already mapped). This is the signature used by BookingController in the current codebase.
     */
    Booking save(Booking booking);

    /**
     * Alternative convenience API that accepts the DTO and performs mapping/validation inside service.
     * Keep if you'd like to call service directly with the DTO from controller.
     */
    Booking saveFromRequest(BookingRequest req);

    Optional<Booking> byId(Long bookingId);
    List<Booking> byCustomer(Long customerId);
    List<Booking> byGarage(Long garageId);
    Booking acceptBooking(Long bookingId, Long garageOwnerId);
    Booking rejectBooking(Long bookingId, Long garageOwnerId, String reason);
}
