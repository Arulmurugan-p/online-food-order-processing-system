package com.foodorder.orderservice.service;

import com.foodorder.orderservice.dto.OrderCreationRequest;
import com.foodorder.orderservice.dto.OrderResponse;
import com.foodorder.orderservice.entity.OrderStatus;

public interface OrderService {
    OrderResponse createOrder(OrderCreationRequest request);
    OrderResponse getOrderById(Long orderId);
    void updateOrderStatus(Long orderId, OrderStatus status);
}
