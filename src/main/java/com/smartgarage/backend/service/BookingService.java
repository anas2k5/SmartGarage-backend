package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingService {

    // --------------------
    // CREATE
    // --------------------
    Booking saveFromRequest(BookingRequest req);

    // --------------------
    // FETCH
    // --------------------
    List<Booking> byCustomer(Long customerId);
    Optional<Booking> byId(Long id);

    // --------------------
    // OWNER
    // --------------------
    List<Booking> getBookingsByGarage(Long garageId, String ownerEmail);

    // --------------------
    // ACTIONS
    // --------------------
    Booking acceptBooking(Long bookingId, Long requesterId, String requesterRole);

    Booking assignMechanic(
            Long bookingId,
            Long mechanicId,
            Long requesterId,
            String requesterRole
    );

    Booking updateBookingStatus(
            Long bookingId,
            String newStatus,
            Long requesterId,
            String requesterRole
    );

    Booking updateEstimatedCost(
            Long bookingId,
            Double estimatedCost,
            Long requesterId,
            String requesterRole
    );

    Booking updateFinalCost(
            Long bookingId,
            Double finalCost,
            Long requesterId,
            String requesterRole
    );
}
