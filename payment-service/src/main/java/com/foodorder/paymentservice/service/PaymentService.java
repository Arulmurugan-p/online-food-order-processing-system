package com.foodorder.paymentservice.service;

import com.foodorder.paymentservice.dto.PaymentRequestEvent;
import com.foodorder.paymentservice.dto.PaymentResponseEvent;

public interface PaymentService {
    PaymentResponseEvent processPayment(PaymentRequestEvent request);
}
