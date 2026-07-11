package com.foodorder.deliveryservice.service;

import com.foodorder.deliveryservice.dto.DeliveryRequestEvent;
import com.foodorder.deliveryservice.dto.DeliveryResponseEvent;
import com.foodorder.deliveryservice.entity.Delivery;
import com.foodorder.deliveryservice.entity.DeliveryStatus;
import com.foodorder.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional
    public DeliveryResponseEvent dispatchDelivery(DeliveryRequestEvent request) {
        log.info("Delivery dispatch requested for Order ID: {} to address: {}", 
                request.getOrderId(), request.getDeliveryAddress());

        Delivery delivery = Delivery.builder()
                .orderId(request.getOrderId())
                .deliveryAddress(request.getDeliveryAddress())
                .status(DeliveryStatus.ASSIGNED)
                .courierName("Courier Jack")
                .build();

        deliveryRepository.save(delivery);

        // Simulate transit
        log.info("Delivery in transit for Order ID: {} via courier {}", 
                request.getOrderId(), delivery.getCourierName());
        delivery.setStatus(DeliveryStatus.DEPARTED);
        deliveryRepository.save(delivery);

        // Simulate successful delivery completion
        log.info("Delivery completed successfully for Order ID: {}", request.getOrderId());
        delivery.setStatus(DeliveryStatus.DELIVERED);
        deliveryRepository.save(delivery);

        return DeliveryResponseEvent.builder()
                .orderId(request.getOrderId())
                .status("DELIVERED")
                .courierName(delivery.getCourierName())
                .build();
    }
}
