package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.*;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
import com.smartgarage.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    // ================= CUSTOMER =================

    @Override
    public CustomerDashboardDTO getCustomerDashboard(Long customerId) {

        userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Booking> bookings =
                bookingRepository.findByCustomerId(customerId);

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

        Double totalSpent = paymentRepository
                .findByBookingCustomerIdAndStatus(
                        customerId,
                        PaymentStatus.SUCCESS
                )
                .stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        List<CustomerBookingSummaryDTO> latestBookings =
                bookings.stream()
                        .sorted(Comparator.comparing(
                                Booking::getBookingTime
                        ).reversed())
                        .limit(5)
                        .map(this::toCustomerSummary)
                        .toList();

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

    private CustomerBookingSummaryDTO toCustomerSummary(
            Booking booking
    ) {
        return CustomerBookingSummaryDTO.builder()
                .bookingId(booking.getId())
                .garageName(
                        booking.getGarage() != null
                                ? booking.getGarage().getName()
                                : null
                )
                .serviceType(
                        booking.getService() != null
                                ? booking.getService().getName()
                                : null
                )
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .finalCost(booking.getFinalCost())
                .build();
    }

    // ================= OWNER =================

    @Override
    public OwnerDashboardDTO getOwnerDashboard(Long ownerId) {

        userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        List<Booking> bookings =
                bookingRepository.findByGarage_Owner_Id(ownerId);

        long total = bookings.size();
        long pending = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();
        long inProgress = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.IN_PROGRESS)
                .count();
        long accepted = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED)
                .count();
        long completed = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();
        long cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        Set<Long> garageIds =
                bookings.stream()
                        .map(b -> b.getGarage().getId())
                        .collect(Collectors.toSet());

        Double totalRevenue = paymentRepository
                .findByBookingGarageOwnerIdAndStatus(
                        ownerId,
                        PaymentStatus.SUCCESS
                )
                .stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        // 🔥 RECENT BOOKINGS
        List<OwnerBookingSummaryDTO> recentBookings =
                bookings.stream()
                        .sorted(Comparator.comparing(
                                Booking::getBookingTime
                        ).reversed())
                        .limit(5)
                        .map(this::toOwnerSummary)
                        .toList();

        // 💳 REAL RECENT PAYMENTS
        List<OwnerPaymentSummaryDTO> recentPayments =
                paymentRepository
                        .findTop5ByBookingGarageOwnerIdAndStatusOrderByCompletedAtDesc(
                                ownerId,
                                PaymentStatus.SUCCESS
                        )
                        .stream()
                        .map(this::toPaymentSummary)
                        .toList();

        return OwnerDashboardDTO.builder()
                .ownerId(ownerId)
                .totalBookings(total)
                .pendingBookings(pending)
                .inProgressBookings(inProgress)
                .acceptedBookings(accepted)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalRevenue(totalRevenue)
                .activeGarages(garageIds.size())
                .recentBookings(recentBookings)
                .recentPayments(recentPayments)
                .build();
    }

    private OwnerBookingSummaryDTO toOwnerSummary(
            Booking booking
    ) {
        return OwnerBookingSummaryDTO.builder()
                .bookingId(booking.getId())
                .customerId(booking.getCustomer().getId())
                .customerEmail(booking.getCustomer().getEmail())
                .garageId(booking.getGarage().getId())
                .garageName(booking.getGarage().getName())
                .serviceType(
                        booking.getService() != null
                                ? booking.getService().getName()
                                : null
                )
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .finalCost(booking.getFinalCost())
                .build();
    }

    private OwnerPaymentSummaryDTO toPaymentSummary(
            Payment payment
    ) {
        return OwnerPaymentSummaryDTO.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .garageName(
                        payment.getBooking()
                                .getGarage()
                                .getName()
                )
                .customerEmail(
                        payment.getBooking()
                                .getCustomer()
                                .getEmail()
                )
                .amount(payment.getAmount())
                .method(payment.getMethod().name())
                .status(payment.getStatus().name())
                .paidAt(payment.getCompletedAt())
                .build();
    }
}
