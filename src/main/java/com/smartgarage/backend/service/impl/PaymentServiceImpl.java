package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.PaymentInitiateRequestDTO;
import com.smartgarage.backend.dto.PaymentResponseDTO;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(
            Long bookingId,
            PaymentInitiateRequestDTO request
    ) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 🔒 STATE VALIDATION
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot pay for cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.PAID) {
            throw new RuntimeException("Booking already paid");
        }

        try {
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount((long) (request.getAmount() * 100))
                            .setCurrency("inr")
                            .putMetadata("bookingId", bookingId.toString())
                            .build();

            PaymentIntent intent =
                    PaymentIntent.create(params);

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

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return toDto(payment, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByCustomer(Long customerId) {
        return paymentRepository
                .findByBookingCustomerId(customerId)
                .stream()
                .map(p -> toDto(p, null))
                .toList();
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
