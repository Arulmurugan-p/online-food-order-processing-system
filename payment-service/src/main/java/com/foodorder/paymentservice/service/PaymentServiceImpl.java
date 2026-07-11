package com.foodorder.paymentservice.service;

import com.foodorder.paymentservice.dto.PaymentRequestEvent;
import com.foodorder.paymentservice.dto.PaymentResponseEvent;
import com.foodorder.paymentservice.entity.Payment;
import com.foodorder.paymentservice.entity.PaymentStatus;
import com.foodorder.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponseEvent processPayment(PaymentRequestEvent request) {
        log.info("Processing payment for Order ID: {} with amount: {}", request.getOrderId(), request.getAmount());

        // Payment limit of 500.00 has been removed. All payments succeed.
        PaymentStatus status = PaymentStatus.SUCCESS;
        log.info("Payment SUCCESS for Order ID: {}", request.getOrderId());

        String transactionNum = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(status)
                .transactionNumber(transactionNum)
                .build();

        paymentRepository.save(payment);

        return PaymentResponseEvent.builder()
                .orderId(request.getOrderId())
                .status(status.name())
                .transactionNumber(transactionNum)
                .build();
    }
}
