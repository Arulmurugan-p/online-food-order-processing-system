package com.foodorder.orderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseEvent {
    private Long orderId;
    private String status;
    private String courierName;
}
