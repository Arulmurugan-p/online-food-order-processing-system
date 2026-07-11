package com.foodorder.deliveryservice.dto;

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
