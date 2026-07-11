package com.foodorder.orderservice.messaging;

import com.foodorder.orderservice.dto.DeliveryResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryResponseConsumer {

    private final RuntimeService runtimeService;

    @JmsListener(destination = "delivery.response.queue")
    public void consumeDeliveryResponse(DeliveryResponseEvent event) {
        log.info("Received Delivery Response for Order ID: {} with status: {}", 
                event.getOrderId(), event.getStatus());

        try {
            // Correlate the delivery response event with the Camunda process instance
            runtimeService.createMessageCorrelation("DeliveryResponseReceived")
                    .processInstanceBusinessKey(event.getOrderId().toString())
                    .correlate();
            
            log.info("Successfully correlated DeliveryResponseReceived to Camunda process for Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to correlate delivery response to Camunda process for Order ID: " + event.getOrderId(), e);
        }
    }
}
