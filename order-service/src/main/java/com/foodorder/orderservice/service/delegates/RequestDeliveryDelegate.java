package com.foodorder.orderservice.service.delegates;

import com.foodorder.orderservice.dto.DeliveryRequestEvent;
import com.foodorder.orderservice.entity.Order;
import com.foodorder.orderservice.entity.OrderStatus;
import com.foodorder.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component("requestDeliveryDelegate")
@RequiredArgsConstructor
@Slf4j
public class RequestDeliveryDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getProcessBusinessKey());
        log.info("Camunda Workflow: Requesting delivery courier for Order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Update status to PREPARING
        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);

        DeliveryRequestEvent deliveryEvent = DeliveryRequestEvent.builder()
                .orderId(orderId)
                .deliveryAddress(order.getDeliveryAddress())
                .build();

        jmsTemplate.convertAndSend("delivery.request.queue", deliveryEvent);
        log.info("Published delivery request for Order ID: {} to ActiveMQ", orderId);
    }
}
