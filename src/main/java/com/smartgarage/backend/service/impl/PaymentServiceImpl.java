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
import com.smartgarage.backend.service.InvoicePdfService;
import com.smartgarage.backend.service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
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
    private final InvoicePdfService invoicePdfService;

    // ----------------------------
    // INITIATE PAYMENT (STRIPE)
    // ----------------------------
    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(Long bookingId, PaymentInitiateRequestDTO request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        try {
            // 1️⃣ CREATE STRIPE PAYMENT INTENT
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount((long) (request.getAmount() * 100)) // ₹ → paise
                            .setCurrency("inr")
                            .putMetadata("bookingId", bookingId.toString())
                            .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // 2️⃣ SAVE OR UPDATE PAYMENT IN DB
            Payment payment = paymentRepository.findByBooking(booking)
                    .orElse(Payment.builder()
                            .booking(booking)
                            .initiatedAt(LocalDateTime.now())
                            .build());

            payment.setAmount(request.getAmount());
            payment.setMethod(request.getMethod());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(intent.getId());

            Payment saved = paymentRepository.save(payment);

            // 3️⃣ RETURN CLIENT SECRET TO FLUTTER
            return PaymentResponseDTO.builder()
                    .id(saved.getId())
                    .bookingId(bookingId)
                    .amount(saved.getAmount())
                    .method(saved.getMethod())
                    .status(saved.getStatus())

                    .transactionId(intent.getId())          // ✅ PI ID
                    .clientSecret(intent.getClientSecret())// ✅ FOR FLUTTER

                    .initiatedAt(saved.getInitiatedAt())
                    .completedAt(null)
                    .build();


        } catch (Exception e) {
            throw new RuntimeException("Stripe payment failed: " + e.getMessage());
        }
    }

    // ----------------------------
    // CONFIRM PAYMENT
    // ----------------------------
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

        // ----------------------------
        // SUCCESS FLOW
        // ----------------------------
        if (request.isSuccess()) {

            // 1️⃣ CREATE / UPDATE INVOICE
            Invoice invoice = invoiceRepository.findByBooking(booking).orElse(null);

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

            // 2️⃣ MARK BOOKING AS PAID
            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);


            // 3️⃣ SEND EMAIL WITH PDF INVOICE
            try {
                String to = booking.getCustomer().getEmail();

                System.out.println(">>> Sending PAYMENT email (PDF invoice) to: " + to);

                String subject = "Payment Successful for Booking #" + booking.getId();
                String text = "Hi,\n\n"
                        + "We have received your payment of ₹" + request.getAmountPaid()
                        + " for booking #" + booking.getId() + ".\n\n"
                        + "Invoice Number: " + invoice.getInvoiceNumber() + "\n\n"
                        + "Your invoice PDF is attached to this email.\n\n"
                        + "Thank you for using Smart Garage.\n\n"
                        + "Regards,\nSmart Garage Team";

                byte[] pdfBytes = invoicePdfService.generateInvoicePdf(bookingId);
                String pdfFilename = "invoice-" + bookingId + ".pdf";

                emailService.sendMailWithAttachment(
                        to,
                        subject,
                        text,
                        pdfBytes,
                        pdfFilename
                );

            } catch (Exception ex) {
                System.out.println("Failed to send payment email: " + ex.getMessage());
            }
        }

        return toPaymentDto(savedPayment);
    }

    // ----------------------------
    // GET PAYMENT STATUS
    // ----------------------------
    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Payment not found for booking"));

        return toPaymentDto(payment);
    }

    // ----------------------------
    // GET INVOICE
    // ----------------------------
    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceByBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        Invoice invoice = invoiceRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Invoice not found for booking"));

        return toInvoiceDto(invoice);
    }

    // ----------------------------
    // HELPERS
    // ----------------------------
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
