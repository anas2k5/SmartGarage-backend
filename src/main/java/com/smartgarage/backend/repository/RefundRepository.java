package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.Payment;
import com.smartgarage.backend.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    boolean existsByPaymentId(Long paymentId);
}
