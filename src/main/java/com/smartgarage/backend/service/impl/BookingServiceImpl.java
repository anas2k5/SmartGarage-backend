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

    public BookingServiceImpl(BookingRepository bookingRepository,
                              GarageRepository garageRepository,
                              VehicleRepository vehicleRepository,
                              UserRepository userRepository,
                              MechanicRepository mechanicRepository,
                              EmailService emailService) {
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
        if (req.getGarageId() == null) throw new IllegalArgumentException("garageId is required");
        if (req.getVehicleId() == null) throw new IllegalArgumentException("vehicleId is required");
        if (req.getCustomerId() == null) throw new IllegalArgumentException("customerId is required");
        if (req.getBookingTime() == null) throw new IllegalArgumentException("bookingTime is required");

        Garage garage = garageRepository.findById(req.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        if (!garage.isActive()) {
            throw new IllegalArgumentException("Garage is not active");
        }

        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (vehicle.getOwner() == null ||
                !vehicle.getOwner().getId().equals(req.getCustomerId())) {
            throw new ForbiddenException("Vehicle does not belong to customer");
        }

        userRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!req.getBookingTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("bookingTime must be in the future");
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

        return bookingRepository.save(booking);
    }

    // -------------------------------------------------
    // FETCH BOOKINGS
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
    public Booking assignMechanic(Long bookingId,
                                  Long mechanicId,
                                  Long requesterId,
                                  String requesterRole) {

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
    // UPDATE BOOKING STATUS (WITH CANCELLATION RULES)
    // -------------------------------------------------
    @Override
    public Booking updateBookingStatus(Long bookingId,
                                       String newStatus,
                                       Long requesterId,
                                       String requesterRole) {

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

            if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
                throw new IllegalStateException("Cannot cancel in-progress booking");
            }

            if (booking.getStatus() == BookingStatus.COMPLETED) {
                throw new IllegalStateException("Cannot cancel completed booking");
            }
        } else {
            if (!isAdmin && !isOwner) {
                throw new ForbiddenException("Only owner or admin can update status");
            }
        }

        booking.setStatus(statusEnum);
        Booking saved = bookingRepository.save(booking);
        System.out.println(
                ">>> STATUS EMAIL TRIGGERED: bookingId=" + booking.getId() +
                        ", status=" + statusEnum +
                        ", to=" + booking.getCustomer().getEmail()
        );


        // 📧 STATUS EMAIL
        try {
            emailService.sendSimpleMail(
                    booking.getCustomer().getEmail(),
                    "Booking Status Updated",
                    "Your booking #" + booking.getId() +
                            " status is now: " + statusEnum
            );
        } catch (Exception e) {
            System.out.println("Status email failed: " + e.getMessage());
        }

        return saved;
    }

    // -------------------------------------------------
    // UPDATE ESTIMATED COST
    // -------------------------------------------------
    @Override
    public Booking updateEstimatedCost(Long bookingId,
                                       Double estimatedCost,
                                       Long requesterId,
                                       String requesterRole) {

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

    // -------------------------------------------------
    // UPDATE FINAL COST
    // -------------------------------------------------
    @Override
    public Booking updateFinalCost(Long bookingId,
                                   Double finalCost,
                                   Long requesterId,
                                   String requesterRole) {

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
