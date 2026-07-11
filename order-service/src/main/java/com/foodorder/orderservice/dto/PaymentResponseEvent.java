package com.foodorder.orderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseEvent {
    private Long orderId;
    private String status;
    private String transactionNumber;
}
