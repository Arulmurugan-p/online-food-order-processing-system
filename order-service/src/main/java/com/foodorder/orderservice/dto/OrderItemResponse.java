package com.foodorder.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
}
