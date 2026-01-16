package com.smartgarage.backend.controller;

import com.google.gson.JsonObject;
import com.smartgarage.backend.dto.PaymentConfirmRequestDTO;
import com.smartgarage.backend.dto.PaymentInitiateRequestDTO;
import com.smartgarage.backend.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments/stripe")
@RequiredArgsConstructor
public class StripePaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    // ============================
    // CREATE STRIPE PAYMENT INTENT
    // ============================
    @PostMapping("/initiate/{bookingId}")
    public ResponseEntity<?> initiateStripePayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentInitiateRequestDTO request
    ) throws Exception {

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount((long) (request.getAmount() * 100)) // rupees → paise
                        .setCurrency("inr")
                        .putMetadata("bookingId", bookingId.toString())
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);

        JsonObject response = new JsonObject();
        response.addProperty("clientSecret", intent.getClientSecret());
        response.addProperty("paymentIntentId", intent.getId());

        return ResponseEntity.ok(response.toString());
    }

    // ============================
    // STRIPE WEBHOOK
    // ============================
    @PostMapping("/webhook")
    public ResponseEntity<?> handleStripeWebhook(HttpServletRequest request) throws Exception {

        String payload = request.getReader()
                .lines()
                .collect(Collectors.joining("\n"));

        String sigHeader = request.getHeader("Stripe-Signature");

        Event event = Webhook.constructEvent(
                payload,
                sigHeader,
                webhookSecret
        );

        if ("payment_intent.succeeded".equals(event.getType())) {

            PaymentIntent intent = (PaymentIntent) event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (intent != null && intent.getMetadata() != null) {

                String bookingIdStr = intent.getMetadata().get("bookingId");

                if (bookingIdStr != null) {
                    Long bookingId = Long.parseLong(bookingIdStr);

                    paymentService.confirmPayment(
                            bookingId,
                            PaymentConfirmRequestDTO.builder()
                                    .transactionId(intent.getId())
                                    .amountPaid(intent.getAmount() / 100.0)
                                    .success(true)
                                    .build()
                    );
                }
            }
        }

        return ResponseEntity.ok().build();
    }
}
