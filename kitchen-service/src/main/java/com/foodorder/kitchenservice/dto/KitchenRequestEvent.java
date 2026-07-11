package com.foodorder.kitchenservice.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenRequestEvent {
    private Long orderId;
    private List<KitchenRequestItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KitchenRequestItem {
        private String itemName;
        private Integer quantity;
    }
}
