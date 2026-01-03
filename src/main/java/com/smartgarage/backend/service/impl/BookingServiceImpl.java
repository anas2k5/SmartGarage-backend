package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.exception.ForbiddenException;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.BookingService;
import com.smartgarage.backend.service.BookingStatusHistoryService;
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
    private final BookingStatusHistoryService historyService;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            GarageRepository garageRepository,
            VehicleRepository vehicleRepository,
            UserRepository userRepository,
            MechanicRepository mechanicRepository,
            EmailService emailService,
            BookingStatusHistoryService historyService
    ) {
        this.bookingRepository = bookingRepository;
        this.garageRepository = garageRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
        this.emailService = emailService;
        this.historyService = historyService;
    }

    // -------------------------------------------------
    // CREATE BOOKING
    // -------------------------------------------------
    @Override
    public Booking saveFromRequest(BookingRequest req) {

        if (req == null) {
            throw new IllegalArgumentException("Request body is required");
        }

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
                .details(req.getDetails())
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);

        historyService.recordStatusChange(
                saved.getId(),
                null,
                BookingStatus.PENDING,
                "SYSTEM"
        );

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
    // ACCEPT BOOKING ✅ (THIS WAS MISSING)
    // -------------------------------------------------
    @Override
    public Booking acceptBooking(
            Long bookingId,
            Long requesterId,
            String requesterRole
    ) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!"ADMIN".equalsIgnoreCase(requesterRole)
                && !booking.getGarage().getOwner().getId().equals(requesterId)) {
            throw new ForbiddenException("Only owner or admin can accept booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be accepted");
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.ACCEPTED);

        Booking saved = bookingRepository.save(booking);

        historyService.recordStatusChange(
                bookingId,
                oldStatus,
                BookingStatus.ACCEPTED,
                requesterRole
        );

        emailService.sendBookingStatusMail(
                booking.getCustomer().getEmail(),
                bookingId,
                BookingStatus.ACCEPTED
        );

        return saved;
    }

    // -------------------------------------------------
    // ASSIGN MECHANIC
    // -------------------------------------------------
    @Override
    public Booking assignMechanic(
            Long bookingId,
            Long mechanicId,
            Long requesterId,
            String requesterRole
    ) {

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
    public Booking updateBookingStatus(
            Long bookingId,
            String newStatus,
            Long requesterId,
            String requesterRole
    ) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        BookingStatus oldStatus = booking.getStatus();
        BookingStatus statusEnum = BookingStatus.valueOf(newStatus);

        booking.setStatus(statusEnum);
        Booking saved = bookingRepository.save(booking);

        historyService.recordStatusChange(
                bookingId,
                oldStatus,
                statusEnum,
                requesterRole
        );

        emailService.sendBookingStatusMail(
                booking.getCustomer().getEmail(),
                bookingId,
                statusEnum
        );

        return saved;
    }

    // -------------------------------------------------
    // COST UPDATES
    // -------------------------------------------------
    @Override
    public Booking updateEstimatedCost(Long bookingId, Double cost, Long requesterId, String role) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        b.setEstimatedCost(cost);
        return bookingRepository.save(b);
    }

    @Override
    public Booking updateFinalCost(Long bookingId, Double cost, Long requesterId, String role) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        b.setFinalCost(cost);
        return bookingRepository.save(b);
    }
}
