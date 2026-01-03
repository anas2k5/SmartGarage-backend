package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.BookingStatusHistoryResponse;
import com.smartgarage.backend.service.BookingStatusHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingStatusHistoryController {

    private final BookingStatusHistoryService bookingStatusHistoryService;

    public BookingStatusHistoryController(
            BookingStatusHistoryService bookingStatusHistoryService
    ) {
        this.bookingStatusHistoryService = bookingStatusHistoryService;
    }

    /**
     * GET booking status history (AUDIT LOG)
     * Example:
     * GET /api/bookings/10/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<BookingStatusHistoryResponse>> getBookingHistory(
            @PathVariable("id") Long bookingId
    ) {
        List<BookingStatusHistoryResponse> history =
                bookingStatusHistoryService.getHistoryForBooking(bookingId);

        return ResponseEntity.ok(history);
    }
}
