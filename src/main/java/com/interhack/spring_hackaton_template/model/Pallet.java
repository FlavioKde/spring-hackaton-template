package com.interhack.spring_hackaton_template.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "load_plan_id")
    private LoadPlan loadPlan;

    private int palletNumber;

    @OneToMany(mappedBy = "pallet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PalletItem> items = new ArrayList<>();

    private double positionXCm;
    private double positionYCm;

    private double widthCm;
    private double lengthCm;
    private double currentHeightCm;
    private double maxHeightCm;

    private double totalWeightKg;
    private double maxWeightKg;

    private int targetStopSequence;

    private boolean loaded;
    private boolean unloaded;

    private String qrCode;
}
