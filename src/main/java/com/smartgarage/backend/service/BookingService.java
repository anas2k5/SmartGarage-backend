package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingService {

    Booking saveFromRequest(BookingRequest req);

    List<Booking> byCustomer(Long customerId);

    List<Booking> byGarage(Long garageId);

    Optional<Booking> byId(Long id);

    // Assign mechanic (Owner/Admin only)
    Booking assignMechanic(Long bookingId, Long mechanicId, Long requesterId, String requesterRole);

    // Update booking status (Owner/Admin only)
    Booking updateBookingStatus(Long bookingId, String newStatus, Long requesterId, String requesterRole);

    // Update estimated cost (Owner/Admin only)
    Booking updateEstimatedCost(Long bookingId, Double estimatedCost, Long requesterId, String requesterRole);

    // Update final cost (Owner/Admin only)
    Booking updateFinalCost(Long bookingId, Double finalCost, Long requesterId, String requesterRole);
}
