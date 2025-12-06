package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.Payment;
import com.smartgarage.backend.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking(Booking booking);

    // all successful payments for a specific customer
    List<Payment> findByBookingCustomerIdAndStatus(Long customerId, PaymentStatus status);

    // all successful payments for all bookings of garages owned by a specific owner
    List<Payment> findByBookingGarageOwnerIdAndStatus(Long ownerId, PaymentStatus status);
}
