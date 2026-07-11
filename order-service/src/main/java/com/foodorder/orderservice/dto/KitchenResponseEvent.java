package com.foodorder.orderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenResponseEvent {
    private Long orderId;
    private String status;
}
