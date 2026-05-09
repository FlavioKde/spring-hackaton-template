package com.interhack.spring_hackaton_template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class DashboardDTO {
    private int totalRoutesToday;
    private int completedRoutes;
    private int activeRoutes;
    private int pendingOrders;
    private int totalDeliveries;
    private int incidences;
    private double totalRevenueToday;
    private double totalFuelCostToday;
    private List<RouteOptimizationResponse.StopDetail> activeStops;
}
