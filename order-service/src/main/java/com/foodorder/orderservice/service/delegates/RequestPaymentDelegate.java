package com.foodorder.orderservice.service.delegates;

import com.foodorder.orderservice.dto.PaymentRequestEvent;
import com.foodorder.orderservice.entity.Order;
import com.foodorder.orderservice.entity.OrderStatus;
import com.foodorder.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component("requestPaymentDelegate")
@RequiredArgsConstructor
@Slf4j
public class RequestPaymentDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getProcessBusinessKey());
        log.info("Camunda Workflow: Requesting payment for Order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Update status to PAYMENT_PENDING or keep as PENDING
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        PaymentRequestEvent paymentEvent = PaymentRequestEvent.builder()
                .orderId(orderId)
                .amount(order.getTotalAmount())
                .build();

        jmsTemplate.convertAndSend("payment.request.queue", paymentEvent);
        log.info("Published payment request for Order ID: {} to ActiveMQ", orderId);
    }
}
