package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.CustomerBookingSummaryDTO;
import com.smartgarage.backend.dto.CustomerDashboardDTO;
import com.smartgarage.backend.dto.OwnerBookingSummaryDTO;
import com.smartgarage.backend.dto.OwnerDashboardDTO;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    // ---------------- CUSTOMER DASHBOARD ----------------

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
                .map(this::toCustomerSummaryDto)
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

    private CustomerBookingSummaryDTO toCustomerSummaryDto(Booking booking) {
        return CustomerBookingSummaryDTO.builder()
                .bookingId(booking.getId())
                .garageName(booking.getGarage() != null ? booking.getGarage().getName() : null)
                .serviceType(booking.getServiceType())
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .finalCost(booking.getFinalCost())
                .build();
    }

    // ---------------- OWNER DASHBOARD ----------------

    @Override
    public OwnerDashboardDTO getOwnerDashboard(Long ownerId) {
        // ensure owner exists
        userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        // all bookings for all garages owned by this owner
        List<Booking> bookings = bookingRepository.findByGarageOwnerId(ownerId);

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

        // active garages = distinct garages that have at least 1 booking
        Set<Long> garageIds = bookings.stream()
                .filter(b -> b.getGarage() != null)
                .map(b -> b.getGarage().getId())
                .collect(Collectors.toSet());
        long activeGarages = garageIds.size();

        // total revenue = sum of successful payments for all bookings under this owner
        List<Payment> successfulPayments =
                paymentRepository.findByBookingGarageOwnerIdAndStatus(ownerId, PaymentStatus.SUCCESS);

        Double totalRevenue = successfulPayments.stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        // recent 5 bookings
        List<OwnerBookingSummaryDTO> recentBookings = bookings.stream()
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .limit(5)
                .map(this::toOwnerSummaryDto)
                .collect(Collectors.toList());

        return OwnerDashboardDTO.builder()
                .ownerId(ownerId)
                .totalBookings(total)
                .pendingBookings(pending)
                .inProgressBookings(inProgress)
                .acceptedBookings(accepted)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalRevenue(totalRevenue)
                .activeGarages(activeGarages)
                .recentBookings(recentBookings)
                .build();
    }

    private OwnerBookingSummaryDTO toOwnerSummaryDto(Booking booking) {
        return OwnerBookingSummaryDTO.builder()
                .bookingId(booking.getId())
                .customerId(booking.getCustomer() != null ? booking.getCustomer().getId() : null)
                .customerEmail(booking.getCustomer() != null ? booking.getCustomer().getEmail() : null)
                .garageId(booking.getGarage() != null ? booking.getGarage().getId() : null)
                .garageName(booking.getGarage() != null ? booking.getGarage().getName() : null)
                .serviceType(booking.getServiceType())
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .finalCost(booking.getFinalCost())
                .build();
    }
}
