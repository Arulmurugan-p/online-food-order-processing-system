package com.foodorder.orderservice.messaging;

import com.foodorder.orderservice.dto.PaymentResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResponseConsumer {

    private final RuntimeService runtimeService;

    @JmsListener(destination = "payment.response.queue")
    public void consumePaymentResponse(PaymentResponseEvent event) {
        log.info("Received Payment Response for Order ID: {} with status: {}", 
                event.getOrderId(), event.getStatus());

        try {
            // Correlate the payment response event with the corresponding Camunda process instance
            runtimeService.createMessageCorrelation("PaymentResponseReceived")
                    .processInstanceBusinessKey(event.getOrderId().toString())
                    .setVariable("paymentStatus", event.getStatus())
                    .correlate();
            
            log.info("Successfully correlated PaymentResponseReceived to Camunda process for Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to correlate payment response to Camunda process for Order ID: " + event.getOrderId(), e);
        }
    }
}
