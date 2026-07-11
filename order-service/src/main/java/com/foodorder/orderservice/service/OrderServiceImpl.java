package com.foodorder.orderservice.service;

import com.foodorder.orderservice.dto.*;
import com.foodorder.orderservice.entity.*;
import com.foodorder.orderservice.exception.OrderNotFoundException;
import com.foodorder.orderservice.repository.OrderRepository;
import com.foodorder.orderservice.repository.OrderStatusHistoryRepository;
import com.foodorder.orderservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final JmsTemplate jmsTemplate;

    @Lazy
    @Autowired
    private SagaSimulatorService sagaSimulatorService;

    @Value("${saga.simulation.enabled:false}")
    private boolean sagaSimulationEnabled;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreationRequest request) {
        return createOrderInternal(request, null);
    }

    @Override
    @Transactional
    public OrderResponse createOrderForUser(OrderCreationRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        return createOrderInternal(request, user);
    }

    private OrderResponse createOrderInternal(OrderCreationRequest request, User user) {
        log.info("Creating order for customer: {}", request.getCustomerName());

        BigDecimal total = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .deliveryAddress(request.getDeliveryAddress())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .user(user)
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

        // Record initial status in history
        addStatusHistory(savedOrder, OrderStatus.PENDING, user != null ? user.getEmail() : "system");

        log.info("Order saved with ID: {}", savedOrder.getId());

        if (sagaSimulationEnabled) {
            log.info("[SagaSimulator] Triggering async simulation for Order ID: {}", savedOrder.getId());
            sagaSimulatorService.simulateSaga(savedOrder.getId());
        } else {
            try {
                jmsTemplate.convertAndSend("order.start.queue", savedOrder.getId());
                log.info("Published order start trigger for Order ID: {}", savedOrder.getId());
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
        addStatusHistory(order, status, "system");
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus, String adminEmail) {
        log.info("Admin {} updating order ID {} to {}", adminEmail, orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        addStatusHistory(saved, newStatus, adminEmail);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.countPending());
        stats.put("cookingOrders", orderRepository.countCooking());
        stats.put("outForDelivery", orderRepository.countOutForDelivery());
        stats.put("deliveredOrders", orderRepository.countDelivered());
        stats.put("cancelledOrders", orderRepository.countCancelledOrRejected());
        stats.put("totalRevenue", orderRepository.sumDeliveredRevenue());
        stats.put("todayOrders", orderRepository.countTodayOrders());
        return stats;
    }

    private void addStatusHistory(Order order, OrderStatus status, String updatedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .updatedBy(updatedBy)
                .timestamp(LocalDateTime.now())
                .build();
        historyRepository.save(history);
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

        List<StatusHistoryResponse> history = historyRepository
                .findByOrderIdOrderByTimestampAsc(order.getId())
                .stream()
                .map(h -> StatusHistoryResponse.builder()
                        .id(h.getId())
                        .status(h.getStatus().name())
                        .updatedBy(h.getUpdatedBy())
                        .timestamp(h.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .deliveryAddress(order.getDeliveryAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .items(items)
                .statusHistory(history)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
