package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ================= ADMIN METRICS =================

    // Total bookings
    long count();

    // Status breakdown
    long countByStatus(BookingStatus status);

    // Revenue sum (PAID / SUCCESS payments only)
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = 'SUCCESS'
    """)
    Double getTotalRevenue();

    // ================= EXISTING =================

    // ✅ FORCE FRESH DB READ (FIXES PAID STATUS ISSUE)
    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId")
    List<Booking> findFreshByCustomerId(
            @Param("customerId") Long customerId
    );

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByGarageId(Long garageId);

    List<Booking> findByGarageIdOrderByBookingTimeDesc(Long garageId);

    List<Booking> findByGarage_Owner_Id(Long ownerId);

    List<Booking> findActiveByCustomerId(Long customerId);
}
