package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingStatusHistoryRepository
        extends JpaRepository<BookingStatusHistory, Long> {

    List<BookingStatusHistory> findByBookingIdOrderByChangedAtAsc(Long bookingId);
}
