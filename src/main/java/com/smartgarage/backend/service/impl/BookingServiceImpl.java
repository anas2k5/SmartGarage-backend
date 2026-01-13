package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.exception.ForbiddenException;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.BookingService;
import com.smartgarage.backend.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final GarageRepository garageRepository;
    private final VehicleRepository vehicleRepository;
    private final MechanicRepository mechanicRepository;
    private final GarageServiceRepository garageServiceRepository;
    private final EmailService emailService;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            GarageRepository garageRepository,
            VehicleRepository vehicleRepository,
            MechanicRepository mechanicRepository,
            GarageServiceRepository garageServiceRepository,
            EmailService emailService
    ) {
        this.bookingRepository = bookingRepository;
        this.garageRepository = garageRepository;
        this.vehicleRepository = vehicleRepository;
        this.mechanicRepository = mechanicRepository;
        this.garageServiceRepository = garageServiceRepository;
        this.emailService = emailService;
    }

    // -------------------------------------------------
    // STATUS TRANSITION RULES
    // -------------------------------------------------
    private static final Set<BookingStatus> PENDING_NEXT =
            EnumSet.of(BookingStatus.ACCEPTED, BookingStatus.CANCELLED);

    private static final Set<BookingStatus> ACCEPTED_NEXT =
            EnumSet.of(BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED);

    private static final Set<BookingStatus> IN_PROGRESS_NEXT =
            EnumSet.of(BookingStatus.COMPLETED);

    private boolean isValidTransition(BookingStatus current, BookingStatus next) {
        return switch (current) {
            case PENDING -> PENDING_NEXT.contains(next);
            case ACCEPTED -> ACCEPTED_NEXT.contains(next);
            case IN_PROGRESS -> IN_PROGRESS_NEXT.contains(next);
            case COMPLETED, CANCELLED -> false;
        };
    }

    // -------------------------------------------------
    // CREATE BOOKING
    // -------------------------------------------------
    @Override
    public Booking saveFromRequest(BookingRequest req) {

        Garage garage = garageRepository.findById(req.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getOwner().getId().equals(req.getCustomerId())) {
            throw new ForbiddenException("Vehicle does not belong to customer");
        }

        if (!req.getBookingTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Booking time must be in the future");
        }

        GarageServiceEntity service = garageServiceRepository
                .findById(req.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getGarage().getId().equals(garage.getId())) {
            throw new IllegalArgumentException("Service does not belong to this garage");
        }

        Booking booking = Booking.builder()
                .garage(garage)
                .vehicle(vehicle)
                .customer(vehicle.getOwner())
                .service(service)
                .bookingTime(req.getBookingTime())
                .status(BookingStatus.PENDING)
                .details(req.getDetails())
                .build();

        Booking saved = bookingRepository.save(booking);

        emailService.sendBookingStatusMail(
                saved.getCustomer().getEmail(),
                saved.getId(),
                saved.getStatus()
        );

        return saved;
    }

    // -------------------------------------------------
    // ACCEPT BOOKING
    // -------------------------------------------------
    @Override
    public Booking acceptBooking(Long bookingId, Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin can accept booking");
        }

        if (!isValidTransition(booking.getStatus(), BookingStatus.ACCEPTED)) {
            throw new IllegalStateException(
                    "Cannot accept booking from status: " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.ACCEPTED);

        Booking saved = bookingRepository.save(booking);

        emailService.sendBookingStatusMail(
                booking.getCustomer().getEmail(),
                saved.getId(),
                BookingStatus.ACCEPTED
        );

        return saved;
    }

    // -------------------------------------------------
    // FETCH
    // -------------------------------------------------
    @Override
    public List<Booking> byCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Booking> byGarage(Long garageId) {
        return bookingRepository.findByGarageId(garageId);
    }

    @Override
    public Optional<Booking> byId(Long id) {
        return bookingRepository.findById(id);
    }

    // -------------------------------------------------
    // ASSIGN MECHANIC
    // -------------------------------------------------
    @Override
    public Booking assignMechanic(Long bookingId, Long mechanicId,
                                  Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin can assign mechanic");
        }

        if (!mechanic.getGarage().getId().equals(booking.getGarage().getId())) {
            throw new IllegalArgumentException("Mechanic does not belong to this garage");
        }

        booking.setMechanic(mechanic);
        return bookingRepository.save(booking);
    }

    // -------------------------------------------------
    // UPDATE STATUS
    // -------------------------------------------------
    @Override
    public Booking updateBookingStatus(Long bookingId, String newStatus,
                                       Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        BookingStatus nextStatus = BookingStatus.valueOf(newStatus);
        BookingStatus currentStatus = booking.getStatus();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);
        boolean isCustomer = booking.getCustomer().getId().equals(requesterId);

        if (nextStatus == BookingStatus.CANCELLED) {
            if (!isAdmin && !isOwner && !isCustomer) {
                throw new ForbiddenException("Not allowed to cancel booking");
            }
        } else {
            if (!isAdmin && !isOwner) {
                throw new ForbiddenException("Only owner or admin can update status");
            }
        }

        if (!isValidTransition(currentStatus, nextStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + currentStatus + " → " + nextStatus
            );
        }

        booking.setStatus(nextStatus);

        Booking saved = bookingRepository.save(booking);

        emailService.sendBookingStatusMail(
                booking.getCustomer().getEmail(),
                saved.getId(),
                nextStatus
        );

        return saved;
    }

    // -------------------------------------------------
    // COST
    // -------------------------------------------------
    @Override
    public Booking updateEstimatedCost(Long bookingId, Double estimatedCost,
                                       Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin can update estimated cost");
        }

        if (estimatedCost < 0) {
            throw new IllegalArgumentException("Estimated cost cannot be negative");
        }

        booking.setEstimatedCost(estimatedCost);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateFinalCost(Long bookingId, Double finalCost,
                                   Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin can update final cost");
        }

        if (finalCost < 0) {
            throw new IllegalArgumentException("Final cost cannot be negative");
        }

        booking.setFinalCost(finalCost);
        return bookingRepository.save(booking);
    }
}
