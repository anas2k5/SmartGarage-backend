package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.CustomerBookingSummaryDTO;
import com.smartgarage.backend.dto.CustomerDashboardDTO;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.BookingStatus;
import com.smartgarage.backend.model.Payment;
import com.smartgarage.backend.model.PaymentStatus;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.repository.PaymentRepository;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    public CustomerDashboardDTO getCustomerDashboard(Long customerId) {
        // ensure customer exists
        userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);

        long total = bookings.size();
        long completed = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();
        long pending = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();
        long ongoing = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.IN_PROGRESS)
                .count();
        long cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        // total spent = sum of successful payments
        List<Payment> successfulPayments =
                paymentRepository.findByBookingCustomerIdAndStatus(customerId, PaymentStatus.SUCCESS);

        Double totalSpent = successfulPayments.stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        // latest 5 bookings, newest first
        List<CustomerBookingSummaryDTO> latestBookings = bookings.stream()
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .limit(5)
                .map(this::toSummaryDto)
                .collect(Collectors.toList());

        return CustomerDashboardDTO.builder()
                .customerId(customerId)
                .totalBookings(total)
                .completedBookings(completed)
                .ongoingBookings(ongoing)
                .pendingBookings(pending)
                .cancelledBookings(cancelled)
                .totalSpent(totalSpent)
                .latestBookings(latestBookings)
                .build();
    }

    private CustomerBookingSummaryDTO toSummaryDto(Booking booking) {
        return CustomerBookingSummaryDTO.builder()
                .bookingId(booking.getId())
                .garageName(booking.getGarage() != null ? booking.getGarage().getName() : null)
                .serviceType(booking.getServiceType())
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .finalCost(booking.getFinalCost())
                .build();
    }
}
