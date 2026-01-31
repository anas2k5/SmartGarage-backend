package com.smartgarage.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminBookingDTO {

    private Long id;
    private String status;
    private String garageName;
    private String customerEmail;
    private LocalDateTime bookingTime;
}
