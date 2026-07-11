package com.foodorder.deliveryservice.service;

import com.foodorder.deliveryservice.dto.DeliveryRequestEvent;
import com.foodorder.deliveryservice.dto.DeliveryResponseEvent;

public interface DeliveryService {
    DeliveryResponseEvent dispatchDelivery(DeliveryRequestEvent request);
}
