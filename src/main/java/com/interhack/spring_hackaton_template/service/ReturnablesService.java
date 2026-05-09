package com.interhack.spring_hackaton_template.service;

import com.interhack.spring_hackaton_template.model.OrderItem;
import com.interhack.spring_hackaton_template.repository.DeliveryNoteRepository;
import com.interhack.spring_hackaton_template.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnablesService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final OrderRepository orderRepository;

    @Data
    @AllArgsConstructor
    @Builder
    public static class ReturnablesSummary {
        private Long routePlanId;
        private int totalReturnablesExpected;
        private int totalReturnablesCollected;
        private int difference;
        private List<ReturnableDetail> details;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class ReturnableDetail {
        private String productName;
        private String clientName;
        private int expected;
        private int collected;
    }

    public ReturnablesSummary getReturnablesSummary(Long routePlanId) {
        var orders = orderRepository.findAll().stream()
                .filter(o -> o.getItems().stream().anyMatch(i -> i.getProduct().isReturnable()))
                .toList();

        int totalExpected = 0;
        int totalCollected = 0;
        List<ReturnableDetail> details = new java.util.ArrayList<>();

        for (var order : orders) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct().isReturnable()) {
                    totalExpected += item.getQuantity();
                    totalCollected += item.getReturnedEmptiesQuantity();
                    details.add(ReturnableDetail.builder()
                            .productName(item.getProduct().getName())
                            .clientName(order.getClient().getName())
                            .expected(item.getQuantity())
                            .collected(item.getReturnedEmptiesQuantity())
                            .build());
                }
            }
        }

        return ReturnablesSummary.builder()
                .routePlanId(routePlanId)
                .totalReturnablesExpected(totalExpected)
                .totalReturnablesCollected(totalCollected)
                .difference(totalExpected - totalCollected)
                .details(details)
                .build();
    }
}
