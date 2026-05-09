package com.interhack.spring_hackaton_template.service;

import com.interhack.spring_hackaton_template.dto.DeliveryConfirmationRequest;
import com.interhack.spring_hackaton_template.dto.DriverRouteDTO;
import com.interhack.spring_hackaton_template.model.*;
import com.interhack.spring_hackaton_template.model.enums.*;
import com.interhack.spring_hackaton_template.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteStopRepository routeStopRepository;
    private final OrderRepository orderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final IncidenceRepository incidenceRepository;
    private final UserRepository userRepository;

    public DriverRouteDTO getDriverRoute(Long driverId) {
        List<RoutePlan> routes = routePlanRepository.findByDriverIdAndStatus(driverId, RouteStatus.IN_PROGRESS);
        if (routes.isEmpty()) {
            routes = routePlanRepository.findByDriverIdAndStatus(driverId, RouteStatus.READY);
        }
        if (routes.isEmpty()) {
            routes = routePlanRepository.findByDriverIdAndStatus(driverId, RouteStatus.OPTIMIZED);
        }
        if (routes.isEmpty()) {
            throw new RuntimeException("No active route found for driver");
        }

        RoutePlan route = routes.getFirst();
        List<RouteStop> stops = routeStopRepository.findByRoutePlanIdOrderBySequenceAsc(route.getId());

        List<DriverRouteDTO.DriverStopDTO> stopDTOs = new ArrayList<>();
        for (RouteStop stop : stops) {
            List<DriverRouteDTO.OrderSummary> orderSummaries = new ArrayList<>();
            for (Order order : stop.getOrders()) {
                List<DriverRouteDTO.OrderItemSummary> itemSummaries = order.getItems().stream()
                        .map(item -> DriverRouteDTO.OrderItemSummary.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .returnable(item.getProduct().isReturnable())
                                .build())
                        .toList();

                orderSummaries.add(DriverRouteDTO.OrderSummary.builder()
                        .orderId(order.getId())
                        .orderCode(order.getOrderCode())
                        .items(itemSummaries)
                        .totalAmount(order.getTotalPrice())
                        .build());
            }

            stopDTOs.add(DriverRouteDTO.DriverStopDTO.builder()
                    .stopId(stop.getId())
                    .sequence(stop.getSequence())
                    .clientName(stop.getClient().getName())
                    .clientAddress(stop.getClient().getAddress())
                    .clientPhone(stop.getClient().getContactPhone())
                    .lat(stop.getClient().getLatitude())
                    .lng(stop.getClient().getLongitude())
                    .status(stop.getDeliveryStatus())
                    .weightToDeliverKg(stop.getWeightToDeliverKg())
                    .orders(orderSummaries)
                    .clusterZone(stop.getClusterZone())
                    .build());
        }

        return DriverRouteDTO.builder()
                .routePlanId(route.getId())
                .routeCode(route.getRouteCode())
                .status(route.getStatus())
                .truckPlate(route.getTruck() != null ? route.getTruck().getPlate() : "N/A")
                .totalDistanceKm(route.getTotalDistanceKm())
                .totalTimeMinutes(route.getTotalTimeMinutes())
                .totalStops(route.getTotalStops())
                .completedStops(route.getCompletedStops())
                .stops(stopDTOs)
                .build();
    }

    @Transactional
    public DeliveryNote confirmDelivery(Long driverId, DeliveryConfirmationRequest request) {
        RouteStop stop = routeStopRepository.findById(request.getRouteStopId())
                .orElseThrow(() -> new RuntimeException("Route stop not found"));
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Update delivered quantities
        if (request.getDeliveredItems() != null) {
            for (var deliveredItem : request.getDeliveredItems()) {
                order.getItems().stream()
                        .filter(oi -> oi.getProduct().getId().equals(deliveredItem.getProductId()))
                        .findFirst()
                        .ifPresent(oi -> oi.setDeliveredQuantity(deliveredItem.getQuantity()));
            }
        }

        // Update returned empties
        int totalReturnables = 0;
        if (request.getReturnedItems() != null) {
            for (var returnedItem : request.getReturnedItems()) {
                order.getItems().stream()
                        .filter(oi -> oi.getProduct().getId().equals(returnedItem.getProductId()))
                        .findFirst()
                        .ifPresent(oi -> oi.setReturnedEmptiesQuantity(returnedItem.getQuantity()));
                totalReturnables += returnedItem.getQuantity();
            }
        }

        DeliveryStatus status;
        if (request.isHasIncidence()) {
            status = DeliveryStatus.INCIDENCE;
            order.setStatus(OrderStatus.INCIDENCE);
        } else {
            boolean allDelivered = order.getItems().stream()
                    .allMatch(oi -> oi.getDeliveredQuantity() >= oi.getQuantity());
            status = allDelivered ? DeliveryStatus.DELIVERED : DeliveryStatus.PARTIAL;
            order.setStatus(allDelivered ? OrderStatus.DELIVERED : OrderStatus.PARTIAL_DELIVERY);
        }
        orderRepository.save(order);

        stop.setDeliveryStatus(status);
        stop.setActualArrival(LocalDateTime.now());
        routeStopRepository.save(stop);

        DeliveryNote note = DeliveryNote.builder()
                .albaranCode("ALB-" + System.currentTimeMillis())
                .routeStop(stop)
                .order(order)
                .driver(driver)
                .status(status)
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(order.getTotalPrice())
                .collectedAmount(request.getCollectedAmount())
                .totalReturnablesCollected(totalReturnables)
                .deliveredAt(LocalDateTime.now())
                .clientSignature(request.getClientSignature())
                .notes(request.getNotes())
                .build();

        if (request.isHasIncidence()) {
            note.setIncidenceType(request.getIncidenceType());
            note.setIncidenceDescription(request.getIncidenceDescription());

            Incidence incidence = Incidence.builder()
                    .routePlan(stop.getRoutePlan())
                    .routeStop(stop)
                    .order(order)
                    .reportedBy(driver)
                    .type(request.getIncidenceType())
                    .description(request.getIncidenceDescription())
                    .resolved(false)
                    .reportedAt(LocalDateTime.now())
                    .build();
            incidenceRepository.save(incidence);
        }

        // Update route progress
        RoutePlan routePlan = stop.getRoutePlan();
        routePlan.setCompletedStops(routePlan.getCompletedStops() + 1);
        if (routePlan.getCompletedStops() >= routePlan.getTotalStops()) {
            routePlan.setStatus(RouteStatus.COMPLETED);
            routePlan.setCompletedAt(LocalDateTime.now());
        }
        routePlanRepository.save(routePlan);

        return deliveryNoteRepository.save(note);
    }

    public List<DeliveryNote> getDeliveryNotesByRoute(Long routeStopId) {
        return deliveryNoteRepository.findByRouteStopId(routeStopId);
    }
}
