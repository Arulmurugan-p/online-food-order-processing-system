package com.foodorder.kitchenservice.service;

import com.foodorder.kitchenservice.dto.KitchenRequestEvent;
import com.foodorder.kitchenservice.dto.KitchenResponseEvent;
import com.foodorder.kitchenservice.entity.KitchenItem;
import com.foodorder.kitchenservice.entity.KitchenOrder;
import com.foodorder.kitchenservice.entity.KitchenStatus;
import com.foodorder.kitchenservice.repository.KitchenOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenServiceImpl implements KitchenService {

    private final KitchenOrderRepository kitchenOrderRepository;

    @Override
    @Transactional
    public KitchenResponseEvent prepareFood(KitchenRequestEvent request) {
        log.info("Kitchen received food prep request for Order ID: {} with {} items", 
                request.getOrderId(), request.getItems().size());

        KitchenOrder kitchenOrder = KitchenOrder.builder()
                .orderId(request.getOrderId())
                .status(KitchenStatus.RECEIVED)
                .build();

        List<KitchenItem> items = request.getItems().stream()
                .map(itemEvent -> KitchenItem.builder()
                        .itemName(itemEvent.getItemName())
                        .quantity(itemEvent.getQuantity())
                        .kitchenOrder(kitchenOrder)
                        .build())
                .collect(Collectors.toList());

        kitchenOrder.setItems(items);
        kitchenOrderRepository.save(kitchenOrder);

        // Simulate kitchen processing
        log.info("Kitchen preparing food for Order ID: {}", request.getOrderId());
        kitchenOrder.setStatus(KitchenStatus.PREPARING);
        kitchenOrderRepository.save(kitchenOrder);

        // Simulate food ready
        log.info("Kitchen finished preparation. Order ID: {} is READY", request.getOrderId());
        kitchenOrder.setStatus(KitchenStatus.READY);
        kitchenOrderRepository.save(kitchenOrder);

        return KitchenResponseEvent.builder()
                .orderId(request.getOrderId())
                .status("PREPARED")
                .build();
    }
}
