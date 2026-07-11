package com.foodorder.orderservice.service.delegates;

import com.foodorder.orderservice.entity.Order;
import com.foodorder.orderservice.entity.OrderStatus;
import com.foodorder.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("completeOrderDelegate")
@RequiredArgsConstructor
@Slf4j
public class CompleteOrderDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getProcessBusinessKey());
        log.info("Camunda Workflow: Completing Order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Update status to DELIVERED
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        log.info("Order ID {} workflow has successfully completed. Food delivered!", orderId);
    }
}
