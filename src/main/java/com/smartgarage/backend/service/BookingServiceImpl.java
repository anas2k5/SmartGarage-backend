package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.BookingStatus;
import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.model.Vehicle;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.repository.GarageRepository;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

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

    /**
     * Save a booking entity (mapped already). Validates nested references.
     */
    @Override
    public Booking save(Booking bookingRequest) {
        if (bookingRequest == null) {
            throw new IllegalArgumentException("Booking request is required");
        }

        // Garage validation
        Long garageId = bookingRequest.getGarage() != null ? bookingRequest.getGarage().getId() : null;
        if (garageId == null) {
            throw new IllegalArgumentException("Garage id is required");
        }
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new IllegalArgumentException("Garage not found"));

        if (!Boolean.TRUE.equals(garage.isActive())) {
            throw new IllegalArgumentException("Garage is not active");
        }

        // Customer validation
        Long customerId = bookingRequest.getCustomer() != null ? bookingRequest.getCustomer().getId() : null;
        if (customerId == null) {
            throw new IllegalArgumentException("Customer id is required");
        }
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Vehicle validation
        Long vehicleId = bookingRequest.getVehicle() != null ? bookingRequest.getVehicle().getId() : null;
        if (vehicleId == null) {
            throw new IllegalArgumentException("Vehicle id is required");
        }
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Verify vehicle belongs to the customer
        if (vehicle.getOwner() == null || !vehicle.getOwner().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Vehicle does not belong to the customer");
        }

        // Build new Booking entity (or update existing)
        Booking booking = new Booking();
        booking.setGarage(garage);
        booking.setCustomer(customer);
        booking.setVehicle(vehicle);
        booking.setServiceType(bookingRequest.getServiceType());
        booking.setDetails(bookingRequest.getDetails());

        // booking time: if request provided (non-null) use it, else use now
        if (bookingRequest.getBookingTime() != null) {
            booking.setBookingTime(bookingRequest.getBookingTime());
        } else {
            booking.setBookingTime(LocalDateTime.now());
        }

        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking saved id={} for customer={} vehicleId={} garageId={}",
                saved.getId(),
                customer.getEmail(),
                vehicle.getId(),
                garage.getId());
        return saved;
    }

    /**
     * Optional: save by mapping from DTO inside service.
     * This expects the DTO contains customer id — adjust if you want authenticated customer passed instead.
     */
    @Override
    public Booking saveFromRequest(BookingRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("BookingRequest is required");
        }

        // Map minimal nested objects (only IDs) and delegate to save(Booking)
        Booking b = new Booking();

        if (req.getGarageId() != null) {
            Garage g = new Garage();
            g.setId(req.getGarageId());
            b.setGarage(g);
        }

        if (req.getVehicleId() != null) {
            Vehicle v = new Vehicle();
            v.setId(req.getVehicleId());
            b.setVehicle(v);
        }

        // If BookingRequest carries the customer id (optional) then set; otherwise controller should set authenticated customer
        if (req.getCustomerId() != null) {
            User u = new User();
            u.setId(req.getCustomerId());
            b.setCustomer(u);
        }

        b.setServiceType(req.getServiceType());
        b.setDetails(req.getDetails());

        // prefer slotStart->bookingTime if present on DTO (assuming DTO uses OffsetDateTime to LocalDateTime conversion)
        if (req.getSlotStart() != null) {
            b.setBookingTime(req.getSlotStart().toLocalDateTime());
        }

        return save(b);
    }

    @Override
    public Optional<Booking> byId(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    @Override
    public List<Booking> byCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Booking> byGarage(Long garageId) {
        return bookingRepository.findByGarageId(garageId);
    }

    @Override
    public Booking acceptBooking(Long bookingId, Long garageOwnerId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (b.getGarage() == null || b.getGarage().getOwner() == null
                || !b.getGarage().getOwner().getId().equals(garageOwnerId)) {
            throw new IllegalArgumentException("Not authorized to accept this booking");
        }
        b.setStatus(BookingStatus.ACCEPTED);
        return bookingRepository.save(b);
    }

    @Override
    public Booking rejectBooking(Long bookingId, Long garageOwnerId, String reason) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (b.getGarage() == null || b.getGarage().getOwner() == null
                || !b.getGarage().getOwner().getId().equals(garageOwnerId)) {
            throw new IllegalArgumentException("Not authorized to reject this booking");
        }
        b.setStatus(BookingStatus.REJECTED);
        String prev = b.getDetails() == null ? "" : b.getDetails() + "\n";
        b.setDetails(prev + "Rejection reason: " + (reason == null ? "" : reason));
        return bookingRepository.save(b);
    }
}
