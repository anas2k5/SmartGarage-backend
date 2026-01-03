package com.smartgarage.backend.model;

public enum BookingStatus {
    PENDING,      // customer created booking
    ACCEPTED,     // owner accepted
    IN_PROGRESS,  // mechanic working
    COMPLETED,    // work + payment done
    CANCELLED     // cancelled by customer/owner
}
