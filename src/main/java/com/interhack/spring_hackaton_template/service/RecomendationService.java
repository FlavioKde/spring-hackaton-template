package com.interhack.spring_hackaton_template.service;

import com.interhack.spring_hackaton_template.model.Client;
import com.interhack.spring_hackaton_template.model.Order;
import com.interhack.spring_hackaton_template.model.Truck;
import com.interhack.spring_hackaton_template.model.enums.OrderStatus;
import com.interhack.spring_hackaton_template.repository.OrderRepository;
import com.interhack.spring_hackaton_template.repository.TruckRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecomendationService {

    private final OrderRepository orderRepository;
    private final TruckRepository truckRepository;
    private final ClusteringService clusteringService;

    @Data
    @AllArgsConstructor
    @Builder
    public static class RouteRecommendation {
        private Long truckId;
        private String truckPlate;
        private double maxPayloadKg;
        private double totalWeightKg;
        private int orderCount;
        private List<Long> orderIds;
        private int estimatedStops;
        private String recommendation;
    }

    public List<RouteRecommendation> recommendRoutes(LocalDate date) {
        List<Order> pendingOrders = orderRepository.findByDeliveryDateAndStatus(date, OrderStatus.PENDING);
        if (pendingOrders.isEmpty()) return List.of();

        List<Truck> trucks = truckRepository.findByAvailableTrue();
        if (trucks.isEmpty()) return List.of();

        List<Client> clients = pendingOrders.stream()
                .map(Order::getClient)
                .distinct()
                .collect(Collectors.toList());

        int numRoutes = Math.min(trucks.size(),
                (int) Math.ceil(pendingOrders.stream().mapToDouble(Order::getTotalWeight).sum()
                        / trucks.getFirst().getMaxPayloadKg()));

        List<ClusteringService.Cluster> clusters = clusteringService.clusterClients(clients,
                Math.max(numRoutes, 1));

        List<RouteRecommendation> recommendations = new ArrayList<>();
        int truckIdx = 0;

        for (ClusteringService.Cluster cluster : clusters) {
            if (truckIdx >= trucks.size()) break;
            Truck truck = trucks.get(truckIdx);

            List<Order> clusterOrders = pendingOrders.stream()
                    .filter(o -> cluster.getClients().contains(o.getClient()))
                    .collect(Collectors.toList());

            double totalWeight = clusterOrders.stream().mapToDouble(Order::getTotalWeight).sum();

            recommendations.add(RouteRecommendation.builder()
                    .truckId(truck.getId())
                    .truckPlate(truck.getPlate())
                    .maxPayloadKg(truck.getMaxPayloadKg())
                    .totalWeightKg(totalWeight)
                    .orderCount(clusterOrders.size())
                    .orderIds(clusterOrders.stream().map(Order::getId).toList())
                    .estimatedStops(cluster.getClients().size())
                    .recommendation(totalWeight > truck.getMaxPayloadKg()
                            ? "OVER CAPACITY - Split required"
                            : String.format("%.0f%% capacity used", totalWeight / truck.getMaxPayloadKg() * 100))
                    .build());

            truckIdx++;
        }

        return recommendations;
    }
}
