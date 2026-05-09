package com.interhack.spring_hackaton_template.service;

import com.interhack.spring_hackaton_template.dto.RouteOptimizationRequest;
import com.interhack.spring_hackaton_template.dto.RouteOptimizationResponse;
import com.interhack.spring_hackaton_template.model.*;
import com.interhack.spring_hackaton_template.model.enums.DeliveryStatus;
import com.interhack.spring_hackaton_template.model.enums.OrderStatus;
import com.interhack.spring_hackaton_template.model.enums.RouteStatus;
import com.interhack.spring_hackaton_template.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteStopRepository routeStopRepository;
    private final OrderRepository orderRepository;
    private final TruckRepository truckRepository;
    private final UserRepository userRepository;
    private final ClusteringService clusteringService;
    private final GoogleMapsService googleMapsService;
    private final FuelService fuelService;
    private final LoadService loadService;

    @Transactional
    public RouteOptimizationResponse optimizeRoute(RouteOptimizationRequest request) {
        List<Order> orders = orderRepository.findAllById(request.getOrderIds());
        Truck truck = truckRepository.findById(request.getTruckId())
                .orElseThrow(() -> new RuntimeException("Truck not found"));
        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        double totalWeight = orders.stream().mapToDouble(Order::getTotalWeight).sum();
        if (totalWeight > truck.getMaxPayloadKg()) {
            throw new RuntimeException("Total weight " + totalWeight + " kg exceeds truck capacity " +
                    truck.getMaxPayloadKg() + " kg");
        }

        List<Client> clients = orders.stream()
                .map(Order::getClient)
                .distinct()
                .collect(Collectors.toList());

        int maxClusters = request.getMaxStops() > 0 ? request.getMaxStops() : clients.size();
        List<ClusteringService.Cluster> clusters = clusteringService.clusterClients(clients, maxClusters);

        List<StopCandidate> stopCandidates = new ArrayList<>();
        for (ClusteringService.Cluster cluster : clusters) {
            List<Order> clusterOrders = orders.stream()
                    .filter(o -> cluster.getClients().contains(o.getClient()))
                    .collect(Collectors.toList());

            Client representative = cluster.getClients().getFirst();
            double weight = clusterOrders.stream().mapToDouble(Order::getTotalWeight).sum();

            stopCandidates.add(new StopCandidate(
                    representative, cluster.getCenterLat(), cluster.getCenterLng(),
                    clusterOrders, cluster.getName(), weight));
        }

        List<StopCandidate> optimizedOrder = solveTSP(
                request.getOriginLat(), request.getOriginLng(), stopCandidates);

        RoutePlan routePlan = RoutePlan.builder()
                .routeCode("RT-" + System.currentTimeMillis())
                .name("Route " + request.getDeliveryDate())
                .deliveryDate(request.getDeliveryDate())
                .driver(driver)
                .truck(truck)
                .status(RouteStatus.OPTIMIZED)
                .originAddress(request.getOriginAddress())
                .originLat(request.getOriginLat())
                .originLng(request.getOriginLng())
                .totalWeightKg(totalWeight)
                .createdAt(LocalDateTime.now())
                .totalStops(optimizedOrder.size())
                .completedStops(0)
                .build();
        routePlan = routePlanRepository.save(routePlan);

        double totalDistance = 0;
        int totalTime = 0;
        double prevLat = request.getOriginLat();
        double prevLng = request.getOriginLng();
        List<RouteOptimizationResponse.StopDetail> stopDetails = new ArrayList<>();

        for (int i = 0; i < optimizedOrder.size(); i++) {
            StopCandidate sc = optimizedOrder.get(i);
            GoogleMapsService.DistanceResult dist = googleMapsService.getDistance(
                    prevLat, prevLng, sc.lat, sc.lng);

            totalDistance += dist.getDistanceKm();
            totalTime += dist.getDurationMinutes();

            RouteStop stop = RouteStop.builder()
                    .routePlan(routePlan)
                    .sequence(i + 1)
                    .client(sc.client)
                    .orders(sc.orders)
                    .clusterZone(sc.clusterName)
                    .distanceFromPreviousKm(dist.getDistanceKm())
                    .timeFromPreviousMinutes(dist.getDurationMinutes())
                    .deliveryStatus(DeliveryStatus.PENDING)
                    .weightToDeliverKg(sc.weightKg)
                    .build();
            routeStopRepository.save(stop);

            sc.orders.forEach(o -> {
                o.setStatus(OrderStatus.ASSIGNED);
                orderRepository.save(o);
            });

            stopDetails.add(RouteOptimizationResponse.StopDetail.builder()
                    .sequence(i + 1)
                    .clientId(sc.client.getId())
                    .clientName(sc.client.getName())
                    .address(sc.client.getAddress())
                    .lat(sc.lat)
                    .lng(sc.lng)
                    .distanceFromPreviousKm(dist.getDistanceKm())
                    .timeFromPreviousMinutes(dist.getDurationMinutes())
                    .weightKg(sc.weightKg)
                    .orderIds(sc.orders.stream().map(Order::getId).toList())
                    .clusterZone(sc.clusterName)
                    .build());

            prevLat = sc.lat;
            prevLng = sc.lng;
        }

        GoogleMapsService.DistanceResult returnDist = googleMapsService.getDistance(
                prevLat, prevLng, request.getOriginLat(), request.getOriginLng());
        totalDistance += returnDist.getDistanceKm();
        totalTime += returnDist.getDurationMinutes();

        var fuelCost = fuelService.calculateFuelCost(totalDistance, truck, totalWeight);

        routePlan.setTotalDistanceKm(totalDistance);
        routePlan.setTotalTimeMinutes(totalTime);
        routePlan.setTotalFuelLiters(fuelCost.getTotalFuelLiters());
        routePlan.setTotalFuelCostEur(fuelCost.getTotalFuelCostEur());
        routePlanRepository.save(routePlan);

        var loadPlan = loadService.generateLoadPlan(routePlan, orders, truck);

        List<RouteOptimizationResponse.ClusterInfo> clusterInfos = clusters.stream()
                .map(c -> RouteOptimizationResponse.ClusterInfo.builder()
                        .zoneName(c.getName())
                        .clientCount(c.getClients().size())
                        .centerLat(c.getCenterLat())
                        .centerLng(c.getCenterLng())
                        .build())
                .toList();

        return RouteOptimizationResponse.builder()
                .routePlanId(routePlan.getId())
                .routeCode(routePlan.getRouteCode())
                .stops(stopDetails)
                .totalDistanceKm(totalDistance)
                .totalTimeMinutes(totalTime)
                .totalFuelLiters(fuelCost.getTotalFuelLiters())
                .totalFuelCostEur(fuelCost.getTotalFuelCostEur())
                .totalWeightKg(totalWeight)
                .totalPallets(loadPlan != null ? loadPlan.getTotalPallets() : 0)
                .loadSummary(loadPlan != null ? RouteOptimizationResponse.LoadSummary.builder()
                        .totalPallets(loadPlan.getTotalPallets())
                        .totalWeightKg(loadPlan.getTotalWeightKg())
                        .maxCapacityKg(truck.getMaxPayloadKg())
                        .usedCapacityPercent(
                                Math.round(loadPlan.getTotalWeightKg() / truck.getMaxPayloadKg() * 10000.0) / 100.0)
                        .safeDistribution(loadPlan.isSafeDistribution())
                        .centerOfGravityX(loadPlan.getCenterOfGravityX())
                        .centerOfGravityY(loadPlan.getCenterOfGravityY())
                        .build() : null)
                .clusters(clusterInfos)
                .build();
    }

    /**
     * Nearest Neighbor heuristic + 2-opt improvement for TSP.
     * Hybrid: considers both distance and delivery order (last stops loaded first).
     */
    private List<StopCandidate> solveTSP(double originLat, double originLng,
                                          List<StopCandidate> candidates) {
        if (candidates.size() <= 1) return new ArrayList<>(candidates);

        int n = candidates.size();
        double[][] distMatrix = new double[n + 1][n + 1];

        for (int i = 0; i < n; i++) {
            StopCandidate a = candidates.get(i);
            distMatrix[0][i + 1] = haversine(originLat, originLng, a.lat, a.lng);
            distMatrix[i + 1][0] = distMatrix[0][i + 1];
            for (int j = i + 1; j < n; j++) {
                StopCandidate b = candidates.get(j);
                double d = haversine(a.lat, a.lng, b.lat, b.lng);
                distMatrix[i + 1][j + 1] = d;
                distMatrix[j + 1][i + 1] = d;
            }
        }

        // Nearest neighbor
        List<Integer> tour = new ArrayList<>();
        boolean[] visited = new boolean[n + 1];
        int current = 0;
        visited[0] = true;

        for (int step = 0; step < n; step++) {
            int nearest = -1;
            double minDist = Double.MAX_VALUE;
            for (int j = 1; j <= n; j++) {
                if (!visited[j] && distMatrix[current][j] < minDist) {
                    minDist = distMatrix[current][j];
                    nearest = j;
                }
            }
            tour.add(nearest);
            visited[nearest] = true;
            current = nearest;
        }

        // 2-opt improvement
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 0; i < tour.size() - 1; i++) {
                for (int j = i + 2; j < tour.size(); j++) {
                    int prevI = (i == 0) ? 0 : tour.get(i - 1);
                    int currI = tour.get(i);
                    int currJ = tour.get(j);
                    int nextJ = (j + 1 < tour.size()) ? tour.get(j + 1) : 0;

                    double oldDist = distMatrix[prevI][currI] + distMatrix[currJ][nextJ];
                    double newDist = distMatrix[prevI][currJ] + distMatrix[currI][nextJ];

                    if (newDist < oldDist - 0.001) {
                        Collections.reverse(tour.subList(i, j + 1));
                        improved = true;
                    }
                }
            }
        }

        List<StopCandidate> result = new ArrayList<>();
        for (int idx : tour) {
            result.add(candidates.get(idx - 1));
        }
        return result;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public RoutePlan getRoutePlan(Long id) {
        return routePlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route plan not found"));
    }

    public List<RoutePlan> getRoutesByDate(java.time.LocalDate date) {
        return routePlanRepository.findByDeliveryDate(date);
    }

    public List<RoutePlan> getRoutesByDriver(Long driverId) {
        return routePlanRepository.findByDriverId(driverId);
    }

    @Transactional
    public RoutePlan startRoute(Long routePlanId) {
        RoutePlan plan = getRoutePlan(routePlanId);
        plan.setStatus(RouteStatus.IN_PROGRESS);
        plan.setStartedAt(LocalDateTime.now());
        return routePlanRepository.save(plan);
    }

    @Transactional
    public RoutePlan completeRoute(Long routePlanId) {
        RoutePlan plan = getRoutePlan(routePlanId);
        plan.setStatus(RouteStatus.COMPLETED);
        plan.setCompletedAt(LocalDateTime.now());
        return routePlanRepository.save(plan);
    }

    private record StopCandidate(Client client, double lat, double lng,
                                  List<Order> orders, String clusterName, double weightKg) {}
}
