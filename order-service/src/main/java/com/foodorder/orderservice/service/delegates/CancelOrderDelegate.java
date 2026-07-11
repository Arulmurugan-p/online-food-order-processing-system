package com.foodorder.orderservice.service.delegates;

import com.foodorder.orderservice.entity.Order;
import com.foodorder.orderservice.entity.OrderStatus;
import com.foodorder.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("cancelOrderDelegate")
@RequiredArgsConstructor
@Slf4j
public class CancelOrderDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getProcessBusinessKey());
        log.warn("Camunda Workflow: Executing Order Cancellation Saga for Order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        log.warn("Order ID {} has been officially CANCELLED due to payment failure.", orderId);
    }
}
