package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.*;
import com.smartgarage.backend.mapper.BookingMapper;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
    // CREATE BOOKING
    // --------------------
    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookingRequest req, Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty())
            return ResponseEntity.status(401).body("Unauthenticated");

        User customer = maybeUser.get();
        req.setCustomerId(customer.getId());

        Booking saved = bookingService.saveFromRequest(req);
        return ResponseEntity.ok(BookingMapper.toResponse(saved));
    }

    // --------------------
    // GET MY BOOKINGS
    // --------------------
    @GetMapping("/me")
    public ResponseEntity<?> getMyBookings(Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty())
            return ResponseEntity.status(401).body("Unauthenticated");

        User me = maybeUser.get();

        return ResponseEntity.ok(
                bookingService.byCustomer(me.getId())
                        .stream()
                        .map(BookingMapper::toResponse)
                        .toList()
        );
    }

    // --------------------
    // ✅ OWNER: GET BOOKINGS BY GARAGE (FIX)
    // --------------------
    @GetMapping("/garage/{garageId}")
    public ResponseEntity<?> getBookingsByGarage(
            @PathVariable Long garageId,
            Principal principal
    ) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty())
            return ResponseEntity.status(401).body("Unauthenticated");

        User owner = maybeUser.get();

        List<BookingResponse> resp = bookingService
                .getBookingsByGarage(garageId, owner.getEmail())
                .stream()
                .map(BookingMapper::toResponse)
                .toList();

        return ResponseEntity.ok(resp);
    }

    // --------------------
    // GET BY ID
    // --------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return bookingService.byId(id)
                .map(b -> ResponseEntity.ok(BookingMapper.toResponse(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    // --------------------
    // UPDATE STATUS
    // --------------------
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest req,
            Principal principal
    ) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty())
            return ResponseEntity.status(401).body("Unauthenticated");

        User actor = maybeUser.get();

        Booking updated = bookingService.updateBookingStatus(
                id,
                req.getStatus(),
                actor.getId(),
                actor.getRole()
        );

        return ResponseEntity.ok(BookingMapper.toResponse(updated));
    }

    // --------------------
    // ASSIGN MECHANIC
    // --------------------
    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignMechanic(
            @PathVariable Long id,
            @RequestParam Long mechanicId,
            Principal principal
    ) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty())
            return ResponseEntity.status(401).body("Unauthenticated");

        User actor = maybeUser.get();

        Booking updated = bookingService.assignMechanic(
                id,
                mechanicId,
                actor.getId(),
                actor.getRole()
        );

        return ResponseEntity.ok(BookingMapper.toResponse(updated));
    }

    // --------------------
    // ACCEPT BOOKING
    // --------------------
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptBooking(@PathVariable Long id, Principal principal) {
        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty())
            return ResponseEntity.status(401).body("Unauthenticated");

        User actor = maybeUser.get();

        Booking updated = bookingService.acceptBooking(
                id,
                actor.getId(),
                actor.getRole()
        );

        return ResponseEntity.ok(BookingMapper.toResponse(updated));
    }
}
