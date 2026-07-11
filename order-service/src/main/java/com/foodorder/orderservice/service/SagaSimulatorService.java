package com.foodorder.orderservice.service;

import com.foodorder.orderservice.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * SagaSimulatorService — used in the deployed (cloud) environment to simulate
 * the full Saga orchestration flow (PENDING → PAID → PREPARING → DELIVERED)
 * without requiring the separate payment/kitchen/delivery microservices.
 *
 * This allows the full order lifecycle to be demonstrated on free hosting platforms
 * where inter-service TCP (ActiveMQ) communication is not available.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaSimulatorService {

    private final OrderService orderService;

    /**
     * Asynchronously advances the order through all Saga states with realistic delays.
     * Runs on a separate thread so it never blocks the HTTP response.
     */
    @Async
    public void simulateSaga(Long orderId) {
        log.info("[SagaSimulator] Starting Saga simulation for Order ID: {}", orderId);

        try {
            // Step 1: Payment processing — simulates payment-service responding
            TimeUnit.SECONDS.sleep(4);
            orderService.updateOrderStatus(orderId, OrderStatus.PAID);
            log.info("[SagaSimulator] Order {} → PAID", orderId);

            // Step 2: Kitchen preparation — simulates kitchen-service responding
            TimeUnit.SECONDS.sleep(6);
            orderService.updateOrderStatus(orderId, OrderStatus.PREPARING);
            log.info("[SagaSimulator] Order {} → PREPARING", orderId);

            // Step 3: Delivery — simulates delivery-service completing the order
            TimeUnit.SECONDS.sleep(8);
            orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
            log.info("[SagaSimulator] Order {} → DELIVERED ✓", orderId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[SagaSimulator] Saga simulation interrupted for Order ID: {}", orderId);
        } catch (Exception e) {
            log.error("[SagaSimulator] Error during Saga simulation for Order ID: {}", orderId, e);
        }
    }
}
