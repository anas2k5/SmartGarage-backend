package com.smartgarage.backend.mapper;

import com.smartgarage.backend.dto.BookingResponse;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.Mechanic;
import com.smartgarage.backend.model.Payment;
import com.smartgarage.backend.repository.PaymentRepository;
import org.springframework.context.ApplicationContext;

public class BookingMapper {

    // ----------------------------
    // SPRING CONTEXT ACCESS
    // ----------------------------
    private static ApplicationContext context;

    public static void setApplicationContext(ApplicationContext ctx) {
        context = ctx;
    }

    private static PaymentRepository paymentRepository() {
        return context.getBean(PaymentRepository.class);
    }

    public static BookingResponse toResponse(Booking b) {
        if (b == null) return null;

        BookingResponse resp = BookingResponse.builder()
                .id(b.getId())
                .garageId(b.getGarage() != null ? b.getGarage().getId() : null)
                .garageName(b.getGarage() != null ? b.getGarage().getName() : null)
                .customerId(b.getCustomer() != null ? b.getCustomer().getId() : null)
                .customerEmail(b.getCustomer() != null ? b.getCustomer().getEmail() : null)
                .vehicleId(b.getVehicle() != null ? b.getVehicle().getId() : null)
                .vehiclePlate(
                        b.getVehicle() != null ? b.getVehicle().getPlateNumber() : null
                )
                .serviceType(
                        b.getService() != null ? b.getService().getName() : null
                )
                .bookingTime(b.getBookingTime())
                .status(b.getStatus() != null ? b.getStatus().name() : null)
                .details(b.getDetails())
                .estimatedCost(b.getEstimatedCost())
                .finalCost(b.getFinalCost())
                .build();

        Mechanic m = b.getMechanic();
        if (m != null) {
            resp.setMechanicId(m.getId());
            resp.setMechanicName(m.getName());
            resp.setMechanicPhone(m.getPhone());
        }

        // ----------------------------
        // 🔥 PAYMENT STATUS MAPPING
        // ----------------------------
        try {
            Payment payment =
                    paymentRepository()
                            .findByBooking(b)
                            .orElse(null);

            resp.setPaymentStatus(
                    payment != null
                            ? payment.getStatus().name()
                            : "PENDING"
            );
        } catch (Exception e) {
            // Failsafe — never break bookings API
            resp.setPaymentStatus("PENDING");
        }

        return resp;
    }
}
