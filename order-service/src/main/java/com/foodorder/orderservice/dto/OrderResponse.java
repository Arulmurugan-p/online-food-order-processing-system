package com.foodorder.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String customerName;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private String status;
    private Long userId;
    private String userEmail;
    private List<OrderItemResponse> items;
    private List<StatusHistoryResponse> statusHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
