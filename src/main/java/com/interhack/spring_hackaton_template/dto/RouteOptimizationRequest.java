package com.interhack.spring_hackaton_template.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RouteOptimizationRequest {
    private List<Long> orderIds;
    private Long truckId;
    private Long driverId;
    private LocalDate deliveryDate;
    private String originAddress;
    private double originLat;
    private double originLng;
    private int maxStops;
    private boolean optimizeFuel;
    private boolean optimizeTime;
}
