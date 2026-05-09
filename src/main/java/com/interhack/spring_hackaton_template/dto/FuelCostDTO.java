package com.interhack.spring_hackaton_template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FuelCostDTO {
    private double distanceKm;
    private double fuelConsumptionPer100Km;
    private double totalFuelLiters;
    private double fuelPricePerLiter;
    private double totalFuelCostEur;
    private double loadedWeightKg;
    private double adjustedConsumption;
}
