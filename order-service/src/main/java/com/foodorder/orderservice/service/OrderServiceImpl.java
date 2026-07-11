package com.foodorder.orderservice.service;

import com.foodorder.orderservice.dto.*;
import com.foodorder.orderservice.entity.Order;
import com.foodorder.orderservice.entity.OrderItem;
import com.foodorder.orderservice.entity.OrderStatus;
import com.foodorder.orderservice.exception.OrderNotFoundException;
import com.foodorder.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final JmsTemplate jmsTemplate;

    @Lazy
    @Autowired
    private SagaSimulatorService sagaSimulatorService;

    @Value("${saga.simulation.enabled:false}")
    private boolean sagaSimulationEnabled;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreationRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerName());

        BigDecimal total = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .deliveryAddress(request.getDeliveryAddress())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .itemName(itemReq.getItemName())
                        .quantity(itemReq.getQuantity())
                        .price(itemReq.getPrice())
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        order.setItems(items);

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved with ID: {}", savedOrder.getId());

        // In deployed (cloud) environments, use the built-in Saga simulator
        // In local dev, rely on JMS → payment/kitchen/delivery services
        if (sagaSimulationEnabled) {
            log.info("[SagaSimulator] Saga simulation enabled — triggering async simulation for Order ID: {}", savedOrder.getId());
            sagaSimulatorService.simulateSaga(savedOrder.getId());
        } else {
            // Publish message to ActiveMQ order.start.queue to trigger Camunda BPMN workflow asynchronously
            try {
                jmsTemplate.convertAndSend("order.start.queue", savedOrder.getId());
                log.info("Published order start trigger to order.start.queue for Order ID: {}", savedOrder.getId());
            } catch (Exception e) {
                log.error("Failed to publish order start event to ActiveMQ", e);
            }
        }

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("Updating order ID {} status to {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .deliveryAddress(order.getDeliveryAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
