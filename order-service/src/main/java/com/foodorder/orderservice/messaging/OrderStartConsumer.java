package com.foodorder.orderservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStartConsumer {

    private final RuntimeService runtimeService;

    @JmsListener(destination = "order.start.queue")
    public void startWorkflow(Long orderId) {
        log.info("Received ActiveMQ start trigger for Order ID: {}", orderId);
        try {
            // Start Camunda BPMN workflow using Order ID as business key
            runtimeService.startProcessInstanceByKey("order-processing-saga", orderId.toString());
            log.info("Successfully started Camunda Process Instance for Order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to start Camunda workflow for Order ID: " + orderId, e);
        }
    }
}
