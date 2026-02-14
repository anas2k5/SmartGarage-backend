package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.InvoicePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final InvoicePdfService invoicePdfService;

    // ================= DOWNLOAD PDF =================
    @GetMapping("/{bookingId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {

        // 🔍 Get logged user email
        String email = authentication.getName();

        // 🔍 Load booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        // 🔐 ROLE-BASED ACCESS CHECK

        boolean isCustomer =
                booking.getCustomer() != null &&
                        booking.getCustomer().getEmail().equals(email);

        boolean isOwner =
                booking.getGarage() != null &&
                        booking.getGarage().getOwner() != null &&
                        booking.getGarage().getOwner().getEmail().equals(email);

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_ADMIN"));

        if (!isCustomer && !isOwner && !isAdmin) {
            throw new RuntimeException("Access Denied");
        }

        // ✅ Generate PDF
        byte[] pdfBytes =
                invoicePdfService.generateInvoicePdf(bookingId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("invoice-" + bookingId + ".pdf")
                        .build()
        );

        return new ResponseEntity<>(
                pdfBytes,
                headers,
                HttpStatus.OK
        );
    }
}
