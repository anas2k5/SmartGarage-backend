package com.smartgarage.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageServiceResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
}
