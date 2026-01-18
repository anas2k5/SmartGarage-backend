package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.Payment;
import com.smartgarage.backend.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking(Booking booking);
    Optional<Payment> findByBookingId(Long bookingId);

    // ✅ CUSTOMER HISTORY
    List<Payment> findByBookingCustomerId(Long customerId);

    // ✅ CUSTOMER TOTAL SPENT
    List<Payment> findByBookingCustomerIdAndStatus(
            Long customerId,
            PaymentStatus status
    );

    // ✅ OWNER REVENUE
    List<Payment> findByBookingGarageOwnerIdAndStatus(
            Long ownerId,
            PaymentStatus status
    );
}
