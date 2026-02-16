package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.PaymentInitiateRequestDTO;
import com.smartgarage.backend.dto.PaymentResponseDTO;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.NotificationService;
import com.smartgarage.backend.service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smartgarage.backend.service.InvoicePdfService;
import com.smartgarage.backend.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    private final InvoicePdfService invoicePdfService;
    private final EmailService emailService;

    // ================= INITIATE PAYMENT =================
    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(
            Long bookingId,
            PaymentInitiateRequestDTO request
    ) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 🔒 STATE VALIDATION (BUSINESS CORRECT)
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot pay for cancelled booking");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Booking must be completed before payment");
        }

        // 🔒 Prevent double payment
        paymentRepository.findByBooking(booking)
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .ifPresent(p -> {
                    throw new RuntimeException("Booking already paid");
                });

        try {
            // ================= STRIPE =================
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount((long) (request.getAmount() * 100))
                            .setCurrency("inr")
                            .putMetadata("bookingId", bookingId.toString())

                            // 🔥 REQUIRED FOR PAYMENT SHEET (PRODUCTION FIX)
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams
                                            .AutomaticPaymentMethods
                                            .builder()
                                            .setEnabled(true)
                                            .build()
                            )

                            .build();


            PaymentIntent intent =
                    PaymentIntent.create(params);

            // ================= PAYMENT ENTITY =================
            Payment payment =
                    paymentRepository.findByBooking(booking)
                            .orElseGet(() -> Payment.builder()
                                    .booking(booking)
                                    .initiatedAt(LocalDateTime.now())
                                    .build()
                            );

            payment.setAmount(request.getAmount());
            payment.setMethod(request.getMethod());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(intent.getId());

            paymentRepository.save(payment);

            return toDto(payment, intent.getClientSecret());

        } catch (Exception e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    // ================= GET PAYMENT =================
    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return toDto(payment, null);
    }

    // ================= CUSTOMER PAYMENTS =================
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByCustomer(Long customerId) {
        return paymentRepository
                .findByBookingCustomerId(customerId)
                .stream()
                .map(p -> toDto(p, null))
                .toList();
    }
    @Override
    @Transactional
    public PaymentResponseDTO confirmPayment(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return toDto(payment, null);
        }

        // 1️⃣ Update payment
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 2️⃣ Update booking
        booking.setPaymentStatus(PaymentStatus.SUCCESS);

// DO NOT change service status
// booking.setStatus(BookingStatus.PAID); ❌ remove

        bookingRepository.save(booking);


        // 3️⃣ Create invoice (if not exists)
        if (invoiceRepository.findByBooking(booking).isEmpty()) {

            Invoice invoice = Invoice.builder()
                    .booking(booking)
                    .payment(payment)
                    .invoiceNumber("INV-" + bookingId)
                    .invoiceDate(LocalDateTime.now())
                    .totalAmount(payment.getAmount())
                    .build();

            invoiceRepository.save(invoice);
        }

        // 4️⃣ Generate PDF + Email
        try {

            byte[] pdf =
                    invoicePdfService.generateInvoicePdf(bookingId);

            emailService.sendMailWithAttachment(
                    booking.getCustomer().getEmail(),
                    "Smart Garage Invoice #" + bookingId,
                    "Dear Customer,\n\n" +
                            "Thank you for your payment.\n" +
                            "Please find attached your invoice.\n\n" +
                            "Smart Garage Team",
                    pdf,
                    "invoice-" + bookingId + ".pdf"
            );

        } catch (Exception e) {

            System.out.println(
                    "Email failed but payment success: "
                            + e.getMessage()
            );
        }

        return toDto(payment, null);
    }

    // ================= HELPER =================
    private PaymentResponseDTO toDto(Payment p, String clientSecret) {
        return PaymentResponseDTO.builder()
                .id(p.getId())
                .bookingId(p.getBooking().getId())
                .amount(p.getAmount())
                .method(p.getMethod())
                .status(p.getStatus())
                .transactionId(p.getTransactionId())
                .clientSecret(clientSecret)
                .initiatedAt(p.getInitiatedAt())
                .completedAt(p.getCompletedAt())
                .build();
    }
}
