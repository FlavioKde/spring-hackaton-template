package com.interhack.spring_hackaton_template.model;

import com.interhack.spring_hackaton_template.model.enums.IncidenceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_plan_id")
    private RoutePlan routePlan;

    @ManyToOne
    @JoinColumn(name = "route_stop_id")
    private RouteStop routeStop;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "reported_by_id")
    private User reportedBy;

    @Enumerated(EnumType.STRING)
    private IncidenceType type;

    private String description;

    private boolean resolved;

    private String resolution;

    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
}
