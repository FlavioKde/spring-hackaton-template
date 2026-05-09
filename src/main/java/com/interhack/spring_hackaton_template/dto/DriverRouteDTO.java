package com.interhack.spring_hackaton_template.dto;

import com.interhack.spring_hackaton_template.model.enums.DeliveryStatus;
import com.interhack.spring_hackaton_template.model.enums.RouteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class DriverRouteDTO {
    private Long routePlanId;
    private String routeCode;
    private RouteStatus status;
    private String truckPlate;
    private double totalDistanceKm;
    private int totalTimeMinutes;
    private int totalStops;
    private int completedStops;
    private List<DriverStopDTO> stops;

    @Data
    @AllArgsConstructor
    @Builder
    public static class DriverStopDTO {
        private Long stopId;
        private int sequence;
        private String clientName;
        private String clientAddress;
        private String clientPhone;
        private double lat;
        private double lng;
        private DeliveryStatus status;
        private String estimatedArrival;
        private double weightToDeliverKg;
        private List<OrderSummary> orders;
        private String clusterZone;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class OrderSummary {
        private Long orderId;
        private String orderCode;
        private List<OrderItemSummary> items;
        private double totalAmount;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class OrderItemSummary {
        private Long productId;
        private String productName;
        private int quantity;
        private boolean returnable;
    }
}
