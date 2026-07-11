package com.foodorder.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestEvent {
    private Long orderId;
    private BigDecimal amount;
}
