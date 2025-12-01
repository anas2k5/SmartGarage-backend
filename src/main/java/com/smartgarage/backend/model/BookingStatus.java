package com.smartgarage.backend.model;

public enum BookingStatus {
    PENDING,    // when a booking is created
    ACCEPTED,   // when garage accepts the booking
    REJECTED,   // when garage rejects the booking
    COMPLETED,  // when the work is done
    CANCELLED   // when customer cancels
}
