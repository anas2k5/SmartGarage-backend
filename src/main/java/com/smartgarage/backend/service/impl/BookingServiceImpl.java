package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.exception.ForbiddenException;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.repository.GarageRepository;
import com.smartgarage.backend.repository.MechanicRepository;
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
    private final MechanicRepository mechanicRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              GarageRepository garageRepository,
                              VehicleRepository vehicleRepository,
                              UserRepository userRepository,
                              MechanicRepository mechanicRepository) {
        this.bookingRepository = bookingRepository;
        this.garageRepository = garageRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
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

    // -----------------------------
    // New methods (assign/update)
    // -----------------------------

    /**
     * Assign a mechanic to booking.
     *
     * Only the garage owner (of the booking's garage) or ADMIN can assign a mechanic.
     * Mechanic must belong to the same garage as the booking.
     */
    @Override
    public Booking assignMechanic(Long bookingId, Long mechanicId, Long requesterId, String requesterRole) {
        if (bookingId == null) throw new IllegalArgumentException("bookingId is required");
        if (mechanicId == null) throw new IllegalArgumentException("mechanicId is required");
        if (requesterId == null) throw new IllegalArgumentException("requesterId is required");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found"));

        // authorize: only garage owner or ADMIN can assign
        User garageOwner = booking.getGarage().getOwner();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = garageOwner != null && garageOwner.getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only the garage owner or admin can assign a mechanic");
        }

        // mechanic must belong to same garage
        if (mechanic.getGarage() == null || !mechanic.getGarage().getId().equals(booking.getGarage().getId())) {
            throw new IllegalArgumentException("Mechanic does not belong to the booking's garage");
        }

        booking.setMechanic(mechanic);

        // optionally change status (commented out)
        // booking.setStatus(BookingStatus.IN_PROGRESS);

        return bookingRepository.save(booking);
    }

    /**
     * Update booking status (owner or admin only for most statuses).
     *
     * For CANCELLED:
     * - Allowed for ADMIN, garage OWNER, or the CUSTOMER
     * - Only when current status is PENDING or ACCEPTED
     */
    @Override
    public Booking updateBookingStatus(Long bookingId, String newStatus, Long requesterId, String requesterRole) {
        if (bookingId == null) throw new IllegalArgumentException("bookingId is required");
        if (newStatus == null || newStatus.isBlank()) throw new IllegalArgumentException("status is required");
        if (requesterId == null) throw new IllegalArgumentException("requesterId is required");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // validate status string matches enum
        BookingStatus statusEnum;
        try {
            statusEnum = BookingStatus.valueOf(newStatus);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid booking status: " + newStatus);
        }

        User garageOwner = booking.getGarage().getOwner();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = garageOwner != null && garageOwner.getId().equals(requesterId);
        boolean isCustomer = booking.getCustomer() != null
                && booking.getCustomer().getId().equals(requesterId);

        // ----- CANCELLATION RULES -----
        if (statusEnum == BookingStatus.CANCELLED) {

            // who can cancel?
            if (!isAdmin && !isOwner && !isCustomer) {
                throw new ForbiddenException("Only the customer, garage owner or admin can cancel this booking");
            }

            // when can cancel?
            BookingStatus current = booking.getStatus();
            if (current == BookingStatus.IN_PROGRESS) {
                throw new IllegalStateException("Cannot cancel a booking that is already in progress");
            }
            if (current == BookingStatus.COMPLETED) {
                throw new IllegalStateException("Cannot cancel a completed booking");
            }
            if (current == BookingStatus.CANCELLED) {
                throw new IllegalStateException("Booking is already cancelled");
            }
            // PENDING or ACCEPTED are allowed → fall through to set status below
        } else {
            // ----- OTHER STATUS CHANGES -----
            // Only garage owner or ADMIN can change non-cancellation statuses
            if (!isAdmin && !isOwner) {
                throw new ForbiddenException("Only the garage owner or admin can change booking status");
            }
        }

        booking.setStatus(statusEnum);
        return bookingRepository.save(booking);
    }

    /**
     * Update estimated cost of booking.
     *
     * Only garage owner or ADMIN may update estimated cost.
     */
    @Override
    public Booking updateEstimatedCost(Long bookingId, Double estimatedCost, Long requesterId, String requesterRole) {
        if (bookingId == null) throw new IllegalArgumentException("bookingId is required");
        if (estimatedCost == null) throw new IllegalArgumentException("estimatedCost is required");
        if (requesterId == null) throw new IllegalArgumentException("requesterId is required");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // authorize: only garage owner or ADMIN can update estimated cost
        User garageOwner = booking.getGarage().getOwner();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = garageOwner != null && garageOwner.getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only the garage owner or admin can update estimated cost");
        }

        if (estimatedCost < 0) {
            throw new IllegalArgumentException("estimatedCost must be non-negative");
        }

        booking.setEstimatedCost(estimatedCost);
        return bookingRepository.save(booking);
    }

    /**
     * Update final cost of booking.
     *
     * Only garage owner or ADMIN may update final cost.
     * Typically used when work is completed and final invoice is known.
     */
    @Override
    public Booking updateFinalCost(Long bookingId, Double finalCost, Long requesterId, String requesterRole) {
        if (bookingId == null) throw new IllegalArgumentException("bookingId is required");
        if (finalCost == null) throw new IllegalArgumentException("finalCost is required");
        if (requesterId == null) throw new IllegalArgumentException("requesterId is required");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // authorize: only garage owner or ADMIN can update final cost
        User garageOwner = booking.getGarage().getOwner();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = garageOwner != null && garageOwner.getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only the garage owner or admin can update final cost");
        }

        if (finalCost < 0) {
            throw new IllegalArgumentException("finalCost must be non-negative");
        }

        booking.setFinalCost(finalCost);

        // optional: you might want to mark booking COMPLETED when final cost is set
        // booking.setStatus(BookingStatus.COMPLETED);

        return bookingRepository.save(booking);
    }
}
