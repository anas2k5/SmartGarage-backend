package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ✅ FORCE FRESH DB READ (FIXES PAID STATUS ISSUE)
    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId")
    List<Booking> findFreshByCustomerId(
            @Param("customerId") Long customerId
    );

    // ---------------- EXISTING ----------------
    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByGarageId(Long garageId);

    List<Booking> findByGarageIdOrderByBookingTimeDesc(Long garageId);

    List<Booking> findByGarage_Owner_Id(Long ownerId);

    List<Booking> findActiveByCustomerId(Long customerId);
}
