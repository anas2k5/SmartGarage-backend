package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.dto.BookingResponse;
import com.smartgarage.backend.dto.UpdateBookingStatusRequest;
import com.smartgarage.backend.mapper.BookingMapper;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    /**
     * Create a booking. Authenticated user's id is attached to request (client must NOT send customerId).
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookingRequest req, Principal principal) {
        // require authentication
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        // find user by email (principal.name)
        Optional<User> maybeUser = userRepository.findByEmail(principal.getName());
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("Authenticated user not found");
        }
        User customer = maybeUser.get();

        // Attach authenticated user's id to request so service validation passes
        req.setCustomerId(customer.getId());

        try {
            Booking saved = bookingService.saveFromRequest(req);
            BookingResponse resp = BookingMapper.toResponse(saved);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            // validation error from service (missing/invalid ids etc.)
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            // permission/ownership related errors
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to create booking: " + ex.getMessage());
        }
    }

    /**
     * Get bookings for a customer. Only the authenticated customer may view their own bookings.
     */
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getByCustomer(@PathVariable("id") Long customerId, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        Optional<User> maybeUser = userRepository.findByEmail(principal.getName());
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("Authenticated user not found");
        }
        User me = maybeUser.get();

        // authorization: allow only the same customer to view their bookings
        if (!me.getId().equals(customerId)) {
            return ResponseEntity.status(403).body("Forbidden: cannot view other user's bookings");
        }

        List<Booking> list = bookingService.byCustomer(customerId);
        List<BookingResponse> resp = list.stream().map(BookingMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    /**
     * Get booking by id (authenticated users only). Returns BookingResponse.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        Optional<Booking> maybe = bookingService.byId(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking b = maybe.get();
        BookingResponse resp = BookingMapper.toResponse(b);

        // (Optional) You can add access check here to ensure only customer/garage owner can view
        return ResponseEntity.ok(resp);
    }

    /**
     * Owner/Admin only: Update booking status.
     * Body example: { "status": "ACCEPTED" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long bookingId,
                                          @Valid @RequestBody UpdateBookingStatusRequest req,
                                          Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        Optional<User> maybeUser = userRepository.findByEmail(principal.getName());
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("Authenticated user not found");
        }
        User actor = maybeUser.get();

        try {
            Booking updated = bookingService.updateBookingStatus(bookingId, req.getStatus(), actor.getId(), actor.getRole());
            BookingResponse resp = BookingMapper.toResponse(updated);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException | com.smartgarage.backend.exception.ForbiddenException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update booking status: " + ex.getMessage());
        }
    }
}
