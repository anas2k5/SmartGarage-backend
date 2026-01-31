package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.BookingStatus;
import com.smartgarage.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminBookingController {

    private final BookingRepository bookingRepository;

    // 🔍 View all bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAll() {
        return ResponseEntity.ok(
                bookingRepository.findAll()
        );
    }

    // 🔍 Filter by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getByStatus(
            @PathVariable BookingStatus status
    ) {
        return ResponseEntity.ok(
                bookingRepository.findAll()
                        .stream()
                        .filter(b -> b.getStatus() == status)
                        .toList()
        );
    }

    // 🚫 Force cancel booking (ADMIN override)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> forceCancel(
            @PathVariable Long id
    ) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found: " + id)
                );

        // 🛡️ Prevent cancelling already PAID bookings
        if (b.getStatus() == BookingStatus.PAID) {
            return ResponseEntity
                    .badRequest()
                    .body("Cannot cancel a PAID booking. Refund process required.");
        }

        b.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(b);

        return ResponseEntity.ok("Booking force cancelled");
    }
}
