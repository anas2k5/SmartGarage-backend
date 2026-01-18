package com.smartgarage.backend.model;

public enum BookingStatus {
    PENDING,      // customer created booking
    ACCEPTED,     // owner accepted
    IN_PROGRESS, // mechanic working
    COMPLETED,   // work completed (waiting for payment)
    PAID,        // customer paid
    CANCELLED    // cancelled by customer/owner
}
