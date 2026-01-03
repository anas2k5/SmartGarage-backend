package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.BookingStatusHistoryResponse;
import com.smartgarage.backend.model.BookingStatus;

import java.util.List;

public interface BookingStatusHistoryService {

    void recordStatusChange(
            Long bookingId,
            BookingStatus oldStatus,
            BookingStatus newStatus,
            String changedBy
    );

    List<BookingStatusHistoryResponse> getHistoryForBooking(Long bookingId);
}
