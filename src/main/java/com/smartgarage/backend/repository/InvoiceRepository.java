package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Invoice;
import com.smartgarage.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByBooking(Booking booking);
}
