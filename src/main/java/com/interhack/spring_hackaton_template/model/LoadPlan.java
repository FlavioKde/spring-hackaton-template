package com.interhack.spring_hackaton_template.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "load_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "route_plan_id")
    private RoutePlan routePlan;

    @OneToMany(mappedBy = "loadPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pallet> pallets = new ArrayList<>();

    private double totalWeightKg;
    private int totalPallets;

    private double centerOfGravityX;
    private double centerOfGravityY;

    private double weightBalanceFrontPercent;
    private double weightBalanceRearPercent;
    private double weightBalanceLeftPercent;
    private double weightBalanceRightPercent;

    private boolean safeDistribution;

    private String loadingInstructions;
}
