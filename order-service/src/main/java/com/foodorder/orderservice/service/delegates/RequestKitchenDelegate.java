package com.foodorder.orderservice.service.delegates;

import com.foodorder.orderservice.dto.KitchenRequestEvent;
import com.foodorder.orderservice.entity.Order;
import com.foodorder.orderservice.entity.OrderStatus;
import com.foodorder.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("requestKitchenDelegate")
@RequiredArgsConstructor
@Slf4j
public class RequestKitchenDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getProcessBusinessKey());
        log.info("Camunda Workflow: Requesting kitchen preparation for Order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Update status to PAID as payment succeeded
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        List<KitchenRequestEvent.KitchenRequestItem> items = order.getItems().stream()
                .map(item -> KitchenRequestEvent.KitchenRequestItem.builder()
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        KitchenRequestEvent kitchenEvent = KitchenRequestEvent.builder()
                .orderId(orderId)
                .items(items)
                .build();

        jmsTemplate.convertAndSend("kitchen.request.queue", kitchenEvent);
        log.info("Published kitchen request for Order ID: {} to ActiveMQ", orderId);
    }
}
