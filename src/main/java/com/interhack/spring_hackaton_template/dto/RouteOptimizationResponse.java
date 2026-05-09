package com.interhack.spring_hackaton_template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class RouteOptimizationResponse {
    private Long routePlanId;
    private String routeCode;
    private List<StopDetail> stops;
    private double totalDistanceKm;
    private int totalTimeMinutes;
    private double totalFuelLiters;
    private double totalFuelCostEur;
    private double totalWeightKg;
    private int totalPallets;
    private LoadSummary loadSummary;
    private List<ClusterInfo> clusters;

    @Data
    @AllArgsConstructor
    @Builder
    public static class StopDetail {
        private int sequence;
        private Long clientId;
        private String clientName;
        private String address;
        private double lat;
        private double lng;
        private double distanceFromPreviousKm;
        private int timeFromPreviousMinutes;
        private String estimatedArrival;
        private double weightKg;
        private List<Long> orderIds;
        private String clusterZone;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class LoadSummary {
        private int totalPallets;
        private double totalWeightKg;
        private double maxCapacityKg;
        private double usedCapacityPercent;
        private boolean safeDistribution;
        private double centerOfGravityX;
        private double centerOfGravityY;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class ClusterInfo {
        private String zoneName;
        private int clientCount;
        private double centerLat;
        private double centerLng;
    }
}
