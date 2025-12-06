package com.smartgarage.backend.model;

public enum BookingStatus {
    PENDING,      // customer created booking
    ACCEPTED,     // owner accepted
    IN_PROGRESS,  // mechanic working / service ongoing
    COMPLETED,    // work + payment done
    CANCELLED     // cancelled by customer/owner
}
