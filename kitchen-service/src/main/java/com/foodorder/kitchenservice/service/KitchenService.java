package com.foodorder.kitchenservice.service;

import com.foodorder.kitchenservice.dto.KitchenRequestEvent;
import com.foodorder.kitchenservice.dto.KitchenResponseEvent;

public interface KitchenService {
    KitchenResponseEvent prepareFood(KitchenRequestEvent request);
}
