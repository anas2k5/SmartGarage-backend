package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.exception.ForbiddenException;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.repository.GarageRepository;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.repository.VehicleRepository;
import com.smartgarage.backend.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final GarageRepository garageRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              GarageRepository garageRepository,
                              VehicleRepository vehicleRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.garageRepository = garageRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Booking saveFromRequest(BookingRequest req) {
        // Validate required fields
        if (req == null) throw new IllegalArgumentException("Request body is required");
        if (req.getGarageId() == null) throw new IllegalArgumentException("garageId is required");
        if (req.getVehicleId() == null) throw new IllegalArgumentException("vehicleId is required");
        if (req.getCustomerId() == null) throw new IllegalArgumentException("customerId is required");
        if (req.getBookingTime() == null) throw new IllegalArgumentException("bookingTime is required");

        // fetch garage
        Garage garage = garageRepository.findById(req.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        if (!garage.isActive()) {
            throw new IllegalArgumentException("Garage is not active");
        }

        // fetch vehicle
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // check ownership of vehicle
        if (vehicle.getOwner() == null || !vehicle.getOwner().getId().equals(req.getCustomerId())) {
            throw new ForbiddenException("Vehicle does not belong to authenticated customer");
        }

        // check customer exists
        userRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // booking time must be in the future
        LocalDateTime now = LocalDateTime.now();
        if (req.getBookingTime().isBefore(now) || req.getBookingTime().isEqual(now)) {
            throw new IllegalArgumentException("bookingTime must be in the future");
        }

        // create booking entity
        Booking booking = Booking.builder()
                .garage(garage)
                .vehicle(vehicle)
                .customer(vehicle.getOwner())
                .serviceType(req.getServiceType())
                .bookingTime(req.getBookingTime())
                .status(BookingStatus.PENDING)
                .details(req.getDetails())
                .build();

        // save and return
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> byCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> byGarage(Long garageId) {
        return bookingRepository.findByGarageId(garageId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> byId(Long id) {
        return bookingRepository.findById(id);
    }

    /**
     * Update booking status. Only the garage owner or ADMIN can update.
     *
     * @param bookingId     id of booking to update
     * @param newStatus     target status string (must be enum name)
     * @param requesterId   id of authenticated user making the request
     * @param requesterRole role string of requester (e.g., "OWNER", "ADMIN")
     * @return updated booking
     */
    @Override
    public Booking updateBookingStatus(Long bookingId, String newStatus, Long requesterId, String requesterRole) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Ensure garage & owner exist
        Garage garage = booking.getGarage();
        if (garage == null || garage.getOwner() == null) {
            throw new IllegalArgumentException("Booking does not have a valid garage/owner");
        }

        Long ownerId = garage.getOwner().getId();

        boolean isOwner = ownerId != null && ownerId.equals(requesterId);
        boolean isAdmin = requesterRole != null && requesterRole.equalsIgnoreCase("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Only the garage owner or admin can change booking status");
        }

        BookingStatus statusEnum;
        try {
            statusEnum = BookingStatus.valueOf(newStatus);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status value: " + newStatus);
        }

        // Optional: simple transition guard (prevent changes once COMPLETED)
        BookingStatus old = booking.getStatus();
        if (old == BookingStatus.COMPLETED && statusEnum != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot change status after completion");
        }

        booking.setStatus(statusEnum);
        Booking saved = bookingRepository.save(booking);

        // TODO: trigger notification/email if desired (e.g., when ACCEPTED or COMPLETED)

        return saved;
    }
}
