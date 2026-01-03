package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.RefundResponseDTO;
import com.smartgarage.backend.exception.ConflictException;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.repository.PaymentRepository;
import com.smartgarage.backend.repository.RefundRepository;
import com.smartgarage.backend.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Override
    public RefundResponseDTO processRefund(Long bookingId, String reason) {

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found for booking " + bookingId
                        )
                );

        if (refundRepository.existsByPaymentId(payment.getId())) {
            throw new ConflictException("Refund already processed");
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .amount(payment.getAmount())
                .reason(reason)
                .status(RefundStatus.REFUNDED)
                .refundedAt(LocalDateTime.now())
                .build();

        Refund saved = refundRepository.save(refund);

        return RefundResponseDTO.builder()
                .refundId(saved.getId())
                .bookingId(bookingId)
                .paymentId(payment.getId())
                .amount(saved.getAmount())
                .reason(saved.getReason())
                .status(saved.getStatus())
                .refundedAt(saved.getRefundedAt())
                .build();
    }
}
