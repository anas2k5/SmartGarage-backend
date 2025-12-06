package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Payment;
import com.smartgarage.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
}
