package com.interhack.spring_hackaton_template.model;

import com.interhack.spring_hackaton_template.model.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "route_stop")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_plan_id")
    private RoutePlan routePlan;

    private int sequence;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToMany
    @JoinTable(
            name = "route_stop_orders",
            joinColumns = @JoinColumn(name = "route_stop_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    private String clusterZone;

    private double distanceFromPreviousKm;
    private int timeFromPreviousMinutes;

    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    private double weightToDeliverKg;
    private double weightToPickupKg;

    private String notes;
}
