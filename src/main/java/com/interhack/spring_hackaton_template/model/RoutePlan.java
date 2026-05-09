package com.interhack.spring_hackaton_template.model;

import com.interhack.spring_hackaton_template.model.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "route_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String routeCode;

    private String name;

    private LocalDate deliveryDate;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "truck_id")
    private Truck truck;

    @OneToMany(mappedBy = "routePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<RouteStop> stops = new ArrayList<>();

    @OneToOne(mappedBy = "routePlan", cascade = CascadeType.ALL)
    private LoadPlan loadPlan;

    @Enumerated(EnumType.STRING)
    private RouteStatus status;

    private double totalDistanceKm;
    private int totalTimeMinutes;
    private double totalFuelLiters;
    private double totalFuelCostEur;
    private double totalWeightKg;

    private String originAddress;
    private double originLat;
    private double originLng;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private int totalStops;
    private int completedStops;
}
