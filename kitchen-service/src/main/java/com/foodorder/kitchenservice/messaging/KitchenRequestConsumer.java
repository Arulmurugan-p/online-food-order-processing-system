package com.foodorder.kitchenservice.messaging;

import com.foodorder.kitchenservice.dto.KitchenRequestEvent;
import com.foodorder.kitchenservice.dto.KitchenResponseEvent;
import com.foodorder.kitchenservice.service.KitchenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenRequestConsumer {

    private final KitchenService kitchenService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "kitchen.request.queue")
    public void consumeKitchenRequest(KitchenRequestEvent request) {
        log.info("Received Kitchen Prep Request for Order ID: {}", request.getOrderId());

        try {
            // Introduce artificial delay to slow down tracking updates for UI visibility
            Thread.sleep(4000);
            KitchenResponseEvent response = kitchenService.prepareFood(request);
            jmsTemplate.convertAndSend("kitchen.response.queue", response);
            log.info("Processed and sent kitchen response for Order ID: {} to kitchen.response.queue", request.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process kitchen request for Order ID: " + request.getOrderId(), e);
        }
    }
}
