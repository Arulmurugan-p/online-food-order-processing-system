package com.foodorder.orderservice.controller;

import com.foodorder.orderservice.dto.OrderCreationRequest;
import com.foodorder.orderservice.dto.OrderResponse;
import com.foodorder.orderservice.entity.OrderStatus;
import com.foodorder.orderservice.repository.UserRepository;
import com.foodorder.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    // ─── Health ─────────────────────────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("UP");
    }

    // ─── Admin Stats ─────────────────────────────────────────────────────────
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(orderService.getAdminStats());
    }

    // ─── Create Order (User) ─────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid OrderCreationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received request to create order for: {}", request.getCustomerName());
        OrderResponse response;
        if (userDetails != null) {
            response = orderService.createOrderForUser(request, userDetails.getUsername());
        } else {
            response = orderService.createOrder(request);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ─── Get All Orders (Admin) ───────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OrderResponse> orders = orderService.getAllOrders(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(orders);
    }

    // ─── Get Order By ID ─────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetch order ID: {} for user: {}", id, userDetails != null ? userDetails.getUsername() : "anon");
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // ─── Admin: Approve ───────────────────────────────────────────────────────
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> approveOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(orderService.updateOrderStatusByAdmin(id, OrderStatus.APPROVED, admin.getUsername()));
    }

    // ─── Admin: Reject ────────────────────────────────────────────────────────
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> rejectOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(orderService.updateOrderStatusByAdmin(id, OrderStatus.REJECTED, admin.getUsername()));
    }

    // ─── Admin: Verify Payment ────────────────────────────────────────────────
    @PutMapping("/{id}/verify-payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> verifyPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(orderService.updateOrderStatusByAdmin(id, OrderStatus.PAYMENT_VERIFIED, admin.getUsername()));
    }

    // ─── Admin: Start Cooking ─────────────────────────────────────────────────
    @PutMapping("/{id}/start-cooking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> startCooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(orderService.updateOrderStatusByAdmin(id, OrderStatus.COOKING, admin.getUsername()));
    }

    // ─── Admin: Food Ready ────────────────────────────────────────────────────
    @PutMapping("/{id}/food-ready")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> foodReady(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(orderService.updateOrderStatusByAdmin(id, OrderStatus.READY, admin.getUsername()));
    }

    // ─── Admin: Start Delivery ────────────────────────────────────────────────
    @PutMapping("/{id}/start-delivery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> startDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(orderService.updateOrderStatusByAdmin(id, OrderStatus.OUT_FOR_DELIVERY, admin.getUsername()));
    }

    // ─── Admin: Deliver ───────────────────────────────────────────────────────
    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> deliverOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(orderService.updateOrderStatusByAdmin(id, OrderStatus.DELIVERED, admin.getUsername()));
    }
}
