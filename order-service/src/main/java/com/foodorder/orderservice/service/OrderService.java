package com.foodorder.orderservice.service;

import com.foodorder.orderservice.dto.OrderCreationRequest;
import com.foodorder.orderservice.dto.OrderResponse;
import com.foodorder.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface OrderService {
    OrderResponse createOrder(OrderCreationRequest request);
    OrderResponse createOrderForUser(OrderCreationRequest request, String userEmail);
    OrderResponse getOrderById(Long orderId);
    void updateOrderStatus(Long orderId, OrderStatus status);
    OrderResponse updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus, String adminEmail);
    List<OrderResponse> getOrdersByUser(Long userId);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    Map<String, Object> getAdminStats();
}
