package com.foodorder.orderservice.messaging;

import com.foodorder.orderservice.dto.KitchenResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenResponseConsumer {

    private final RuntimeService runtimeService;

    @JmsListener(destination = "kitchen.response.queue")
    public void consumeKitchenResponse(KitchenResponseEvent event) {
        log.info("Received Kitchen Response for Order ID: {} with status: {}", 
                event.getOrderId(), event.getStatus());

        try {
            // Correlate the kitchen response event with the Camunda process instance
            runtimeService.createMessageCorrelation("KitchenResponseReceived")
                    .processInstanceBusinessKey(event.getOrderId().toString())
                    .correlate();
            
            log.info("Successfully correlated KitchenResponseReceived to Camunda process for Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to correlate kitchen response to Camunda process for Order ID: " + event.getOrderId(), e);
        }
    }
}
