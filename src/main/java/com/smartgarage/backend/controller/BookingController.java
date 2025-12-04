package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.BookingRequest;
import com.smartgarage.backend.dto.BookingResponse;
import com.smartgarage.backend.dto.UpdateBookingStatusRequest;
import com.smartgarage.backend.dto.UpdateEstimatedCostRequest;
import com.smartgarage.backend.dto.UpdateFinalCostRequest;
import com.smartgarage.backend.mapper.BookingMapper;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    // --------------------
    // Helper
    // --------------------
    private Optional<User> getAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(principal.getName());
    }

    // --------------------
    // Endpoints
    // --------------------
    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookingRequest req, Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) return ResponseEntity.status(401).body("Unauthenticated");

        User customer = maybeUser.get();
        req.setCustomerId(customer.getId());

        try {
            Booking saved = bookingService.saveFromRequest(req);
            BookingResponse resp = BookingMapper.toResponse(saved);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to create booking: " + ex.getMessage());
        }
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getByCustomer(@PathVariable("id") Long customerId, Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) return ResponseEntity.status(401).body("Unauthenticated");

        User me = maybeUser.get();
        if (!me.getId().equals(customerId)) {
            return ResponseEntity.status(403).body("Forbidden: cannot view other user's bookings");
        }

        List<Booking> list = bookingService.byCustomer(customerId);
        List<BookingResponse> resp = list.stream().map(BookingMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id, Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) return ResponseEntity.status(401).body("Unauthenticated");

        Optional<Booking> maybe = bookingService.byId(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking b = maybe.get();
        BookingResponse resp = BookingMapper.toResponse(b);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long bookingId,
                                          @Valid @RequestBody UpdateBookingStatusRequest req,
                                          Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) return ResponseEntity.status(401).body("Unauthenticated");
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

    /**
     * Owner/Admin only: assign a mechanic to a booking
     * Usage: PUT /api/bookings/{id}/assign?mechanicId=1
     */
    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignMechanic(@PathVariable("id") Long bookingId,
                                            @RequestParam("mechanicId") Long mechanicId,
                                            Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) return ResponseEntity.status(401).body("Unauthenticated");
        User actor = maybeUser.get();

        try {
            Booking updated = bookingService.assignMechanic(bookingId, mechanicId, actor.getId(), actor.getRole());
            return ResponseEntity.ok(BookingMapper.toResponse(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException | com.smartgarage.backend.exception.ForbiddenException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to assign mechanic: " + ex.getMessage());
        }
    }

    /**
     * Owner/Admin only: update estimated cost
     * PUT /api/bookings/{id}/estimate
     * Body: { "estimatedCost": 3500.0 }
     */
    @PutMapping("/{id}/estimate")
    public ResponseEntity<?> updateEstimatedCost(@PathVariable("id") Long bookingId,
                                                 @Valid @RequestBody UpdateEstimatedCostRequest req,
                                                 Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) return ResponseEntity.status(401).body("Unauthenticated");
        User actor = maybeUser.get();

        try {
            Booking updated = bookingService.updateEstimatedCost(bookingId, req.getEstimatedCost(), actor.getId(), actor.getRole());
            return ResponseEntity.ok(BookingMapper.toResponse(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException | com.smartgarage.backend.exception.ForbiddenException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update estimated cost: " + ex.getMessage());
        }
    }

    /**
     * Owner/Admin only: update final cost
     * PUT /api/bookings/{id}/final-cost
     * Body: { "finalCost": 4200.0 }
     */
    @PutMapping("/{id}/final-cost")
    public ResponseEntity<?> updateFinalCost(@PathVariable("id") Long bookingId,
                                             @Valid @RequestBody UpdateFinalCostRequest req,
                                             Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) return ResponseEntity.status(401).body("Unauthenticated");
        User actor = maybeUser.get();

        try {
            Booking updated = bookingService.updateFinalCost(bookingId, req.getFinalCost(), actor.getId(), actor.getRole());
            return ResponseEntity.ok(BookingMapper.toResponse(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException | com.smartgarage.backend.exception.ForbiddenException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update final cost: " + ex.getMessage());
        }
    }
}
