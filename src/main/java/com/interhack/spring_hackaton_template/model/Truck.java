package com.interhack.spring_hackaton_template.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "truck")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String plate;

    private String model;

    private double maxWeightKg;

    private double cargoLengthCm;
    private double cargoWidthCm;
    private double cargoHeightCm;

    private double fuelConsumptionPer100Km;

    private double tare;

    @Builder.Default
    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "current_driver_id")
    private User currentDriver;

    public double getCargoVolumeCm3() {
        return cargoLengthCm * cargoWidthCm * cargoHeightCm;
    }

    public double getMaxPayloadKg() {
        return maxWeightKg - tare;
    }
}
