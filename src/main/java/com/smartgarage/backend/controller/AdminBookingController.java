package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.AdminBookingDTO;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.BookingStatus;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminBookingController {

    private final BookingRepository bookingRepository;
    private final AuditService auditService;

    // ================= GET ALL BOOKINGS =================
    @GetMapping
    public ResponseEntity<List<AdminBookingDTO>> getAll() {
        List<AdminBookingDTO> result =
                bookingRepository.findAll()
                        .stream()
                        .map(this::mapToDto)
                        .toList();

        return ResponseEntity.ok(result);
    }

    // ================= FILTER BY STATUS =================
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AdminBookingDTO>> getByStatus(
            @PathVariable BookingStatus status
    ) {
        List<AdminBookingDTO> result =
                bookingRepository.findAll()
                        .stream()
                        .filter(b -> b.getStatus() == status)
                        .map(this::mapToDto)
                        .toList();

        return ResponseEntity.ok(result);
    }

    // ================= FORCE CANCEL =================
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> forceCancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return bookingRepository.findById(id)
                .map(booking -> {

                    if (booking.getStatus() == BookingStatus.PAID) {
                        return ResponseEntity
                                .badRequest()
                                .body("Cannot cancel PAID booking. Refund required.");
                    }

                    String oldStatus = booking.getStatus().name();

                    booking.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);

                    // 🔥 AUDIT LOG
                    auditService.log(
                            null,
                            userDetails.getUsername(),
                            "ADMIN",
                            "STATUS_CHANGE",
                            "BOOKING",
                            booking.getId(),
                            oldStatus,
                            "CANCELLED"
                    );

                    return ResponseEntity.ok("Booking cancelled successfully");
                })
                .orElseGet(() ->
                        ResponseEntity.badRequest().body("Booking not found")
                );
    }

    // ================= DTO MAPPER =================
    private AdminBookingDTO mapToDto(Booking b) {
        return AdminBookingDTO.builder()
                .id(b.getId())
                .status(b.getStatus().name())
                .bookingTime(b.getBookingTime())
                .garageName(
                        b.getGarage() != null
                                ? b.getGarage().getName()
                                : "—"
                )
                .customerEmail(
                        b.getCustomer() != null
                                ? b.getCustomer().getEmail()
                                : "—"
                )
                .build();
    }
}
