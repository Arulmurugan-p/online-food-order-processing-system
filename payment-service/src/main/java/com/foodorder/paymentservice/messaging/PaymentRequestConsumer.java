package com.foodorder.paymentservice.messaging;

import com.foodorder.paymentservice.dto.PaymentRequestEvent;
import com.foodorder.paymentservice.dto.PaymentResponseEvent;
import com.foodorder.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestConsumer {

    private final PaymentService paymentService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "payment.request.queue")
    public void consumePaymentRequest(PaymentRequestEvent request) {
        log.info("Received Payment Request for Order ID: {} with amount: {}", 
                request.getOrderId(), request.getAmount());

        try {
            // Introduce artificial delay to slow down tracking updates for UI visibility
            Thread.sleep(3000);
            PaymentResponseEvent response = paymentService.processPayment(request);
            jmsTemplate.convertAndSend("payment.response.queue", response);
            log.info("Processed and sent payment response for Order ID: {} to payment.response.queue", request.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process payment request for Order ID: " + request.getOrderId(), e);
        }
    }
}
