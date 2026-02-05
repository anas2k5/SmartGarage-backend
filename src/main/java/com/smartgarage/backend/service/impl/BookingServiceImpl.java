package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.exception.ForbiddenException;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.AuditService;
import com.smartgarage.backend.service.BookingService;
import com.smartgarage.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final GarageRepository garageRepository;
    private final VehicleRepository vehicleRepository;
    private final MechanicRepository mechanicRepository;
    private final GarageServiceRepository garageServiceRepository;
    private final JobCardRepository jobCardRepository;   // ✅ ADDED
    private final EmailService emailService;
    private final AuditService auditService;

    // -------------------------------------------------
    // STATUS RULES
    // -------------------------------------------------
    private static final Set<BookingStatus> PENDING_NEXT =
            EnumSet.of(BookingStatus.ACCEPTED, BookingStatus.CANCELLED);

    private static final Set<BookingStatus> ACCEPTED_NEXT =
            EnumSet.of(BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED);

    private static final Set<BookingStatus> IN_PROGRESS_NEXT =
            EnumSet.of(BookingStatus.COMPLETED);

    private static final Set<BookingStatus> COMPLETED_NEXT =
            EnumSet.of(BookingStatus.PAID);

    private boolean isValidTransition(BookingStatus current, BookingStatus next) {
        return switch (current) {
            case PENDING -> PENDING_NEXT.contains(next);
            case ACCEPTED -> ACCEPTED_NEXT.contains(next);
            case IN_PROGRESS -> IN_PROGRESS_NEXT.contains(next);
            case COMPLETED -> COMPLETED_NEXT.contains(next);
            case PAID, CANCELLED -> false;
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

        GarageServiceEntity service = garageServiceRepository.findById(req.getServiceId())
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

        return bookingRepository.save(booking);
    }

    // -------------------------------------------------
    // FETCH
    // -------------------------------------------------
    @Override
    public List<Booking> byCustomer(Long customerId) {
        return bookingRepository.findFreshByCustomerId(customerId);
    }

    @Override
    public Optional<Booking> byId(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    public List<Booking> getBookingsByGarage(Long garageId, String ownerEmail) {

        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        if (!garage.getOwner().getEmail().equals(ownerEmail)) {
            throw new ForbiddenException("Not authorized to view bookings for this garage");
        }

        return bookingRepository.findByGarageIdOrderByBookingTimeDesc(garageId);
    }

    // -------------------------------------------------
    // ACCEPT BOOKING
    // -------------------------------------------------
    @Override
    public Booking acceptBooking(Long bookingId, Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new IllegalStateException("Paid booking cannot be changed");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin can accept booking");
        }

        if (!isValidTransition(booking.getStatus(), BookingStatus.ACCEPTED)) {
            throw new IllegalStateException("Invalid transition");
        }

        BookingStatus old = booking.getStatus();
        booking.setStatus(BookingStatus.ACCEPTED);
        Booking saved = bookingRepository.save(booking);

        auditService.log(
                AuditModule.BOOKING_MANAGEMENT,
                requesterId,
                booking.getCustomer().getEmail(),
                requesterRole,
                "STATUS_CHANGE",
                "BOOKING",
                bookingId,
                old.name(),
                BookingStatus.ACCEPTED.name()
        );

        return saved;
    }

    // -------------------------------------------------
    // ASSIGN MECHANIC + JOBCARD CREATION
    // -------------------------------------------------
    // -------------------------------------------------
// ASSIGN MECHANIC + AUTO CREATE JOBCARD
// -------------------------------------------------
    @Override
    public Booking assignMechanic(
            Long bookingId,
            Long mechanicId,
            Long requesterId,
            String requesterRole
    ) {

        // ----------------------------
        // LOAD BOOKING
        // ----------------------------
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found")
                );

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new IllegalStateException(
                    "Paid booking cannot be changed"
            );
        }

        // ----------------------------
        // LOAD MECHANIC
        // ----------------------------
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Mechanic not found")
                );

        // ----------------------------
        // SECURITY CHECK
        // ----------------------------
        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(requesterRole);

        boolean isOwner =
                booking.getGarage()
                        .getOwner()
                        .getId()
                        .equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException(
                    "Only owner or admin can assign mechanic"
            );
        }

        // ----------------------------
        // GARAGE VALIDATION
        // ----------------------------
        if (!mechanic.getGarage().getId()
                .equals(booking.getGarage().getId())) {

            throw new IllegalArgumentException(
                    "Mechanic does not belong to this garage"
            );
        }

        // ----------------------------
        // ASSIGN MECHANIC
        // ----------------------------
        String oldMechanic =
                booking.getMechanic() != null
                        ? booking.getMechanic().getName()
                        : "NONE";

        booking.setMechanic(mechanic);

        Booking savedBooking =
                bookingRepository.save(booking);

        // =================================================
        // 🔥 AUTO CREATE JOBCARD (MAIN FIX)
        // =================================================
        boolean jobCardExists =
                jobCardRepository
                        .findByBookingId(bookingId)
                        .isPresent();

        if (!jobCardExists) {

            JobCard jobCard = JobCard.builder()
                    .booking(savedBooking)
                    .mechanic(mechanic)
                    .status(JobCardStatus.OPEN)
                    .laborCost(0.0)
                    .partsCost(0.0)
                    .notes("Auto-created on mechanic assignment")
                    .build();

            jobCardRepository.save(jobCard);
        }

        // ----------------------------
        // AUDIT LOG
        // ----------------------------
        auditService.log(
                AuditModule.BOOKING_MANAGEMENT,
                requesterId,
                booking.getCustomer().getEmail(),
                requesterRole,
                "ASSIGN_MECHANIC",
                "BOOKING",
                bookingId,
                oldMechanic,
                mechanic.getName()
        );

        return savedBooking;
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

        BookingStatus nextStatus = BookingStatus.valueOf(newStatus);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled");
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new IllegalStateException("Paid booking cannot be changed");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);
        boolean isCustomer = booking.getCustomer().getId().equals(requesterId);

        if (nextStatus == BookingStatus.CANCELLED) {
            if (!isCustomer && !isAdmin && !isOwner) {
                throw new ForbiddenException("Only customer, owner or admin can cancel booking");
            }
        } else {
            if (!isAdmin && !isOwner) {
                throw new ForbiddenException("Only owner or admin can update booking status");
            }
        }

        if (!isValidTransition(booking.getStatus(), nextStatus)) {
            throw new IllegalStateException("Invalid transition");
        }

        BookingStatus old = booking.getStatus();
        booking.setStatus(nextStatus);
        Booking saved = bookingRepository.save(booking);

        auditService.log(
                AuditModule.BOOKING_MANAGEMENT,
                requesterId,
                booking.getCustomer().getEmail(),
                requesterRole,
                "STATUS_CHANGE",
                "BOOKING",
                bookingId,
                old.name(),
                nextStatus.name()
        );

        return saved;
    }

    // -------------------------------------------------
    // UPDATE ESTIMATED COST
    // -------------------------------------------------
    @Override
    public Booking updateEstimatedCost(
            Long bookingId,
            Double estimatedCost,
            Long requesterId,
            String requesterRole
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new IllegalStateException("Paid booking cannot be changed");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin allowed");
        }

        Double old = booking.getEstimatedCost();
        booking.setEstimatedCost(estimatedCost);
        Booking saved = bookingRepository.save(booking);

        auditService.log(
                AuditModule.BOOKING_MANAGEMENT,
                requesterId,
                booking.getCustomer().getEmail(),
                requesterRole,
                "ESTIMATED_COST_UPDATE",
                "BOOKING",
                bookingId,
                String.valueOf(old),
                String.valueOf(estimatedCost)
        );

        return saved;
    }

    // -------------------------------------------------
    // UPDATE FINAL COST
    // -------------------------------------------------
    @Override
    public Booking updateFinalCost(
            Long bookingId,
            Double finalCost,
            Long requesterId,
            String requesterRole
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new IllegalStateException("Paid booking cannot be changed");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        boolean isOwner = booking.getGarage().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Only owner or admin allowed");
        }

        Double old = booking.getFinalCost();
        booking.setFinalCost(finalCost);
        Booking saved = bookingRepository.save(booking);

        auditService.log(
                AuditModule.BOOKING_MANAGEMENT,
                requesterId,
                booking.getCustomer().getEmail(),
                requesterRole,
                "FINAL_COST_UPDATE",
                "BOOKING",
                bookingId,
                String.valueOf(old),
                String.valueOf(finalCost)
        );

        return saved;
    }
}
