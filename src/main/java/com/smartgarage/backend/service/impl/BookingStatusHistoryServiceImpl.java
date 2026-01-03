package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.dto.BookingStatusHistoryResponse;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.*;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.repository.BookingStatusHistoryRepository;
import com.smartgarage.backend.service.BookingStatusHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingStatusHistoryServiceImpl
        implements BookingStatusHistoryService {

    private final BookingStatusHistoryRepository historyRepository;
    private final BookingRepository bookingRepository;

    public BookingStatusHistoryServiceImpl(
            BookingStatusHistoryRepository historyRepository,
            BookingRepository bookingRepository
    ) {
        this.historyRepository = historyRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void recordStatusChange(
            Long bookingId,
            BookingStatus oldStatus,
            BookingStatus newStatus,
            String changedBy
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        BookingStatusHistory history = BookingStatusHistory.builder()
                .booking(booking)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }

    @Override
    public List<BookingStatusHistoryResponse> getHistoryForBooking(Long bookingId) {

        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Booking not found");
        }

        return historyRepository
                .findByBookingIdOrderByChangedAtAsc(bookingId)
                .stream()
                .map(h -> BookingStatusHistoryResponse.builder()
                        .oldStatus(h.getOldStatus())
                        .newStatus(h.getNewStatus())
                        .changedBy(h.getChangedBy())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
