package com.smartgarage.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.EmailService;
import com.smartgarage.backend.service.InvoicePdfService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/stripe/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;
    private final InvoicePdfService invoicePdfService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        try {
            byte[] raw = request.getInputStream().readAllBytes();
            String payload = new String(raw, StandardCharsets.UTF_8);

            String sigHeader = request.getHeader("Stripe-Signature");

            Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret
            );

            System.out.println("✅ Stripe Event: " + event.getType());

            if ("payment_intent.succeeded".equals(event.getType())) {
                handleSuccessRaw(payload);
            }

            return ResponseEntity.ok("Received");

        } catch (SignatureVerificationException e) {
            System.out.println("❌ Invalid Stripe signature");
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Handled");
        }
    }

    // ================= RAW PARSE SUCCESS HANDLER =================
    @Transactional
    public void handleSuccessRaw(String payload) throws Exception {

        Map<String, Object> root =
                objectMapper.readValue(payload, Map.class);

        Map<String, Object> data =
                (Map<String, Object>) root.get("data");

        Map<String, Object> obj =
                (Map<String, Object>) data.get("object");

        Map<String, String> metadata =
                (Map<String, String>) obj.get("metadata");

        if (metadata == null || !metadata.containsKey("bookingId")) {
            System.out.println("⚠️ Missing bookingId in metadata");
            return;
        }

        Long bookingId = Long.parseLong(metadata.get("bookingId"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            System.out.println("⚠️ Duplicate webhook ignored for booking " + bookingId);
            return;
        }

        // 1️⃣ PAYMENT
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 2️⃣ BOOKING
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.saveAndFlush(booking);

        System.out.println("✅ Booking " + bookingId + " marked as PAID");

        // 3️⃣ INVOICE
        Invoice invoice = invoiceRepository.findByBooking(booking)
                .orElse(
                        Invoice.builder()
                                .booking(booking)
                                .payment(payment)
                                .invoiceNumber(
                                        "INV-" + bookingId + "-" + System.currentTimeMillis()
                                )
                                .invoiceDate(LocalDateTime.now())
                                .totalAmount(payment.getAmount())
                                .build()
                );

        invoiceRepository.save(invoice);

        // 4️⃣ EMAIL (SAFE)
        try {
            byte[] pdf = invoicePdfService.generateInvoicePdf(bookingId);

            emailService.sendMailWithAttachment(
                    booking.getCustomer().getEmail(),
                    "Payment Successful - Booking #" + bookingId,
                    "Your payment was successful. Invoice attached.",
                    pdf,
                    "invoice-" + bookingId + ".pdf"
            );

            System.out.println("📧 Invoice email sent");

        } catch (Exception e) {
            System.out.println("⚠️ Email failed: " + e.getMessage());
        }
    }
}
