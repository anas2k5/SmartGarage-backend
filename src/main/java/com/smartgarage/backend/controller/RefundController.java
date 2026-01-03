package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.RefundResponseDTO;
import com.smartgarage.backend.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/{bookingId}")
    public ResponseEntity<RefundResponseDTO> refundBooking(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "Booking cancelled") String reason
    ) {
        return ResponseEntity.ok(
                refundService.processRefund(bookingId, reason)
        );
    }
}
