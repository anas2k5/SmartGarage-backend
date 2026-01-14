package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.*;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.*;
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

    @Override
    public CustomerDashboardDTO getCustomerDashboard(Long customerId) {

        userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);

        long total = bookings.size();
        long completed = bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long pending = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long ongoing = bookings.stream().filter(b -> b.getStatus() == BookingStatus.IN_PROGRESS).count();
        long cancelled = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        Double totalSpent = paymentRepository
                .findByBookingCustomerIdAndStatus(customerId, PaymentStatus.SUCCESS)
                .stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        List<CustomerBookingSummaryDTO> latestBookings = bookings.stream()
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
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

    private CustomerBookingSummaryDTO toCustomerSummary(Booking booking) {
        return CustomerBookingSummaryDTO.builder()
                .bookingId(booking.getId())
                .garageName(booking.getGarage() != null ? booking.getGarage().getName() : null)
                .serviceType(
                        booking.getService() != null ? booking.getService().getName() : null
                )
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .finalCost(booking.getFinalCost())
                .build();
    }

    @Override
    public OwnerDashboardDTO getOwnerDashboard(Long ownerId) {

        userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        List<Booking> bookings =bookingRepository.findByGarage_Owner_Id(ownerId);


        long total = bookings.size();
        long pending = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long inProgress = bookings.stream().filter(b -> b.getStatus() == BookingStatus.IN_PROGRESS).count();
        long accepted = bookings.stream().filter(b -> b.getStatus() == BookingStatus.ACCEPTED).count();
        long completed = bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long cancelled = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        Set<Long> garageIds = bookings.stream()
                .map(b -> b.getGarage().getId())
                .collect(Collectors.toSet());

        Double totalRevenue = paymentRepository
                .findByBookingGarageOwnerIdAndStatus(ownerId, PaymentStatus.SUCCESS)
                .stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        List<OwnerBookingSummaryDTO> recentBookings = bookings.stream()
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .limit(5)
                .map(this::toOwnerSummary)
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
                .build();
    }

    private OwnerBookingSummaryDTO toOwnerSummary(Booking booking) {
        return OwnerBookingSummaryDTO.builder()
                .bookingId(booking.getId())
                .customerId(booking.getCustomer().getId())
                .customerEmail(booking.getCustomer().getEmail())
                .garageId(booking.getGarage().getId())
                .garageName(booking.getGarage().getName())
                .serviceType(
                        booking.getService() != null ? booking.getService().getName() : null
                )
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .finalCost(booking.getFinalCost())
                .build();
    }
}
