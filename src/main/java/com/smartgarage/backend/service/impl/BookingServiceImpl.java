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
    private final EmailService emailService;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            GarageRepository garageRepository,
            VehicleRepository vehicleRepository,
            UserRepository userRepository,
            MechanicRepository mechanicRepository,
            EmailService emailService
    ) {
        this.bookingRepository = bookingRepository;
        this.garageRepository = garageRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
        this.emailService = emailService;
    }

    // -------------------------------------------------
    // CREATE BOOKING
    // -------------------------------------------------
    @Override
    public Booking saveFromRequest(BookingRequest req) {

        if (req == null) throw new IllegalArgumentException("Request body is required");

        Garage garage = garageRepository.findById(req.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        if (!garage.isActive()) {
            throw new IllegalStateException("Garage is not active");
        }

        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getOwner().getId().equals(req.getCustomerId())) {
            throw new ForbiddenException("Vehicle does not belong to customer");
        }

        if (!req.getBookingTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Booking time must be in the future");
        }

        Booking booking = Booking.builder()
                .garage(garage)
                .vehicle(vehicle)
                .customer(vehicle.getOwner())
                .serviceType(req.getServiceType())
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
    public Booking assignMechanic(Long bookingId, Long mechanicId, Long requesterId, String requesterRole) {

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
    // ✅ OWNER ACCEPT BOOKING (NEW FEATURE)
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

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be accepted");
        }

        booking.setStatus(BookingStatus.ACCEPTED);
        Booking saved = bookingRepository.save(booking);

        emailService.sendBookingStatusMail(
                booking.getCustomer().getEmail(),
                booking.getId(),
                BookingStatus.ACCEPTED
        );

        return saved;
    }

    // -------------------------------------------------
    // UPDATE STATUS (STRICT RULES)
    // -------------------------------------------------
    @Override
    public Booking updateBookingStatus(Long bookingId, String newStatus, Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        BookingStatus statusEnum = BookingStatus.valueOf(newStatus);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);
        boolean isCustomer = booking.getCustomer().getId().equals(requesterId);

        if (statusEnum == BookingStatus.CANCELLED) {

            if (!isAdmin && !isOwner && !isCustomer) {
                throw new ForbiddenException("Not allowed to cancel booking");
            }

            if (booking.getStatus() == BookingStatus.IN_PROGRESS ||
                    booking.getStatus() == BookingStatus.COMPLETED) {
                throw new IllegalStateException("Cannot cancel booking at this stage");
            }

        } else {
            if (!isAdmin && !isOwner) {
                throw new ForbiddenException("Only owner or admin can update status");
            }

            if (statusEnum == BookingStatus.IN_PROGRESS &&
                    booking.getStatus() != BookingStatus.ACCEPTED) {
                throw new IllegalStateException("Booking must be ACCEPTED before starting");
            }
        }

        booking.setStatus(statusEnum);
        Booking saved = bookingRepository.save(booking);

        emailService.sendBookingStatusMail(
                booking.getCustomer().getEmail(),
                booking.getId(),
                statusEnum
        );

        return saved;
    }

    // -------------------------------------------------
    // COSTS
    // -------------------------------------------------
    @Override
    public Booking updateEstimatedCost(Long bookingId, Double estimatedCost, Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin can update estimated cost");
        }

        booking.setEstimatedCost(estimatedCost);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateFinalCost(Long bookingId, Double finalCost, Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin can update final cost");
        }

        booking.setFinalCost(finalCost);
        return bookingRepository.save(booking);
    }
}
