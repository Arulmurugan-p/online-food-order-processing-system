package com.foodorder.orderservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistoryResponse {
    private Long id;
    private String status;
    private String updatedBy;
    private LocalDateTime timestamp;
}
