package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ✅ Customer sees ALL bookings (including CANCELLED)
    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByGarageId(Long garageId);

    // ✅ REQUIRED FOR OWNER FLOW
    List<Booking> findByGarageIdOrderByBookingTimeDesc(Long garageId);

    List<Booking> findByGarage_Owner_Id(Long ownerId);

    List<Booking> findActiveByCustomerId(Long customerId);
}
