package com.foodorder.paymentservice.dto;

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
