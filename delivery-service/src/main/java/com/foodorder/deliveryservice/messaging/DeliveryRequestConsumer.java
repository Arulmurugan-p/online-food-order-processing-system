package com.foodorder.deliveryservice.messaging;

import com.foodorder.deliveryservice.dto.DeliveryRequestEvent;
import com.foodorder.deliveryservice.dto.DeliveryResponseEvent;
import com.foodorder.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryRequestConsumer {

    private final DeliveryService deliveryService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "delivery.request.queue")
    public void consumeDeliveryRequest(DeliveryRequestEvent request) {
        log.info("Received Delivery Dispatch Request for Order ID: {}", request.getOrderId());

        try {
            // Introduce artificial delay to slow down tracking updates for UI visibility
            Thread.sleep(4000);
            DeliveryResponseEvent response = deliveryService.dispatchDelivery(request);
            jmsTemplate.convertAndSend("delivery.response.queue", response);
            log.info("Processed and sent delivery response for Order ID: {} to delivery.response.queue", request.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process delivery request for Order ID: " + request.getOrderId(), e);
        }
    }
}
