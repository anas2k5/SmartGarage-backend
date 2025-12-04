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
}
