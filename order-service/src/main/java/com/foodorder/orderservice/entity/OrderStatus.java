package com.foodorder.orderservice.entity;

public enum OrderStatus {
    PENDING,
    APPROVED,
    PAYMENT_VERIFIED,
    COOKING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    REJECTED,
    CANCELLED
}
