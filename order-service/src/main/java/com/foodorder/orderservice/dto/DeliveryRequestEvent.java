package com.foodorder.orderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequestEvent {
    private Long orderId;
    private String deliveryAddress;
}
