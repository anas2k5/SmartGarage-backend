package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.InvoiceDTO;
import com.smartgarage.backend.dto.PaymentConfirmRequestDTO;
import com.smartgarage.backend.dto.PaymentInitiateRequestDTO;
import com.smartgarage.backend.dto.PaymentResponseDTO;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.BookingStatus;
import com.smartgarage.backend.model.Invoice;
import com.smartgarage.backend.model.Payment;
import com.smartgarage.backend.model.PaymentStatus;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.repository.InvoiceRepository;
import com.smartgarage.backend.repository.PaymentRepository;
import com.smartgarage.backend.service.EmailService;
import com.smartgarage.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(Long bookingId, PaymentInitiateRequestDTO request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // either existing payment or new one
        Payment payment = paymentRepository.findByBooking(booking)
                .orElse(Payment.builder()
                        .booking(booking)
                        .initiatedAt(LocalDateTime.now())
                        .build());

        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);

        Payment saved = paymentRepository.save(payment);
        return toPaymentDto(saved);
    }

    @Override
    @Transactional
    public PaymentResponseDTO confirmPayment(Long bookingId, PaymentConfirmRequestDTO request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Payment not initiated for this booking"));

        // prevent double confirmation
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment already completed for this booking");
        }

        payment.setTransactionId(request.getTransactionId());
        payment.setAmount(request.getAmountPaid());
        payment.setCompletedAt(LocalDateTime.now());
        payment.setStatus(request.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        Payment savedPayment = paymentRepository.save(payment);

        // If success → create/update invoice + update booking status + send email
        if (request.isSuccess()) {
            // create or update invoice (avoid duplicate key error)
            Invoice invoice = invoiceRepository.findByBooking(booking)
                    .orElse(null);

            if (invoice == null) {
                invoice = Invoice.builder()
                        .booking(booking)
                        .payment(savedPayment)
                        .invoiceNumber(generateInvoiceNumber(booking))
                        .invoiceDate(LocalDateTime.now())
                        .totalAmount(request.getAmountPaid())
                        .build();
            } else {
                invoice.setPayment(savedPayment);
                invoice.setTotalAmount(request.getAmountPaid());
                invoice.setInvoiceDate(LocalDateTime.now());
            }

            invoiceRepository.save(invoice);

            // mark booking as COMPLETED
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);

            // send email to YOUR gmail directly for testing
            try {
                String to = booking.getCustomer().getEmail();

                System.out.println(">>> Sending PAYMENT email to: " + to);

                String subject = "Payment Successful for Booking #" + booking.getId();
                String text = "Hi,\n\n"
                        + "We have received your payment of ₹" + request.getAmountPaid()
                        + " for booking #" + booking.getId() + ".\n"
                        + "Invoice Number: " + invoice.getInvoiceNumber() + "\n\n"
                        + "Thank you for using Smart Garage.\n\n"
                        + "Regards,\nSmart Garage Team";

                emailService.sendSimpleMail(to, subject, text);
            } catch (Exception ex) {
                // avoid breaking payment flow if email fails
                System.out.println("Failed to send payment email: " + ex.getMessage());
            }
        }

        return toPaymentDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Payment not found for booking"));

        return toPaymentDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        Invoice invoice = invoiceRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Invoice not found for booking"));

        return toInvoiceDto(invoice);
    }

    // ---------- private helpers ----------

    private PaymentResponseDTO toPaymentDto(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .initiatedAt(payment.getInitiatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }

    private InvoiceDTO toInvoiceDto(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .bookingId(invoice.getBooking().getId())
                .paymentId(invoice.getPayment().getId())
                .totalAmount(invoice.getTotalAmount())
                .invoiceDate(invoice.getInvoiceDate())
                .build();
    }

    private String generateInvoiceNumber(Booking booking) {
        return "INV-" + booking.getId() + "-" + System.currentTimeMillis();
    }
}
