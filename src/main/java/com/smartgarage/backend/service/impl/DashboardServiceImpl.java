
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
    private final VehicleRepository vehicleRepository;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final GarageRepository garageRepository;
    private final MechanicRepository mechanicRepository; // 🔥 ADDED

    // ================= CUSTOMER =================

    @Override
    public CustomerDashboardDTO getCustomerDashboard(Long customerId) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found"));


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
        Vehicle primaryVehicle = vehicleRepository
                .findFirstByOwnerId(customerId)
                .orElse(null);

        return CustomerDashboardDTO.builder()
                .primaryVehicle(toVehicleDTO(primaryVehicle))
                .customerId(customerId)
                // ✅ ADD THIS
                .customerName(customer.getFullName())
                .totalBookings(total)
                .completedBookings(completed)
                .ongoingBookings(ongoing)
                .pendingBookings(pending)
                .cancelledBookings(cancelled)
                .totalSpent(totalSpent)
                .latestBookings(latestBookings)
                .build();
    }

    private VehicleDTO toVehicleDTO(Vehicle v) {
        if (v == null) return null;

        return VehicleDTO.builder()
                .id(v.getId())
                .make(v.getMake())
                .model(v.getModel())
                .registrationNumber(v.getPlateNumber())
                .build();
    }

    // ================= OWNER =================

    @Override
    public OwnerDashboardDTO getOwnerDashboard(Long ownerId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Owner not found"));

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
                        .map(b -> b.getGarage() != null ? b.getGarage().getId() : null)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        // 🔥 MECHANIC COUNT ADDED
        long totalMechanics =
                mechanicRepository.countByGarageOwnerId(ownerId);

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

        List<OwnerBookingSummaryDTO> recentBookings =
                bookings.stream()
                        .sorted(Comparator.comparing(
                                Booking::getBookingTime
                        ).reversed())
                        .limit(5)
                        .map(this::safeOwnerSummary)
                        .toList();

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
                .ownerName(owner.getFullName())
                .totalBookings(total)
                .pendingBookings(pending)
                .inProgressBookings(inProgress)
                .acceptedBookings(accepted)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalRevenue(totalRevenue)
                .activeGarages(garageIds.size())
                .totalMechanics(totalMechanics) // 🔥 ADDED
                .recentBookings(recentBookings)
                .recentPayments(recentPayments)
                .build();
    }

    // ================= ADMIN (RESTORED) =================

    @Override
    public AdminDashboardDTO getAdminDashboard() {

        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole("CUSTOMER");
        long totalOwners = userRepository.countByRole("OWNER");
        long totalGarages = garageRepository.count();
        long totalBookings = bookingRepository.count();

        long pending = bookingRepository.countByStatus(BookingStatus.PENDING);
        long accepted = bookingRepository.countByStatus(BookingStatus.ACCEPTED);
        long inProgress = bookingRepository.countByStatus(BookingStatus.IN_PROGRESS);
        long completed = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        long paid = bookingRepository.countByStatus(BookingStatus.PAID);

        Double totalRevenue = bookingRepository.getTotalRevenue();

        List<OwnerBookingSummaryDTO> recentBookings =
                bookingRepository.findAll()
                        .stream()
                        .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                        .limit(5)
                        .map(this::safeOwnerSummary)
                        .toList();

        List<OwnerPaymentSummaryDTO> recentPayments =
                paymentRepository
                        .findTop5ByStatusOrderByCompletedAtDesc(PaymentStatus.SUCCESS)
                        .stream()
                        .map(this::toPaymentSummary)
                        .toList();

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalOwners(totalOwners)
                .totalGarages(totalGarages)
                .totalBookings(totalBookings)
                .pendingBookings(pending)
                .acceptedBookings(accepted)
                .inProgressBookings(inProgress)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .paidBookings(paid)
                .totalRevenue(totalRevenue)
                .recentBookings(recentBookings)
                .recentPayments(recentPayments)
                .build();
    }

    // ================= SAFE MAPPERS =================

    private OwnerBookingSummaryDTO safeOwnerSummary(Booking booking) {

        String garageName = null;
        Long garageId = null;

        if (booking.getGarage() != null) {
            garageName = booking.getGarage().getName();
            garageId = booking.getGarage().getId();
        }

        String customerEmail = null;
        Long customerId = null;

        if (booking.getCustomer() != null) {
            customerEmail = booking.getCustomer().getEmail();
            customerId = booking.getCustomer().getId();
        }

        return OwnerBookingSummaryDTO.builder()
                .bookingId(booking.getId())
                .customerId(customerId)
                .customerEmail(customerEmail)
                .garageId(garageId)
                .garageName(garageName)
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
                .bookingId(
                        payment.getBooking() != null
                                ? payment.getBooking().getId()
                                : null
                )
                .garageName(
                        payment.getBooking() != null &&
                                payment.getBooking().getGarage() != null
                                ? payment.getBooking().getGarage().getName()
                                : null
                )
                .customerEmail(
                        payment.getBooking() != null &&
                                payment.getBooking().getCustomer() != null
                                ? payment.getBooking().getCustomer().getEmail()
                                : null
                )
                .amount(payment.getAmount())
                .method(
                        payment.getMethod() != null
                                ? payment.getMethod().name()
                                : null
                )
                .status(
                        payment.getStatus() != null
                                ? payment.getStatus().name()
                                : null
                )
                .paidAt(payment.getCompletedAt())
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

}
