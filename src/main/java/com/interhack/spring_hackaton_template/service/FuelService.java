package com.interhack.spring_hackaton_template.service;

import com.interhack.spring_hackaton_template.dto.FuelCostDTO;
import com.interhack.spring_hackaton_template.model.Truck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FuelService {

    @Value("${truck.default.fuel-price-eur-per-l}")
    private double defaultFuelPrice;

    public FuelCostDTO calculateFuelCost(double distanceKm, Truck truck, double loadedWeightKg) {
        double baseConsumption = truck.getFuelConsumptionPer100Km();

        double loadFactor = loadedWeightKg / truck.getMaxPayloadKg();
        double adjustedConsumption = baseConsumption * (1.0 + 0.15 * loadFactor);

        double totalFuel = (distanceKm / 100.0) * adjustedConsumption;
        double totalCost = totalFuel * defaultFuelPrice;

        return FuelCostDTO.builder()
                .distanceKm(distanceKm)
                .fuelConsumptionPer100Km(baseConsumption)
                .adjustedConsumption(Math.round(adjustedConsumption * 100.0) / 100.0)
                .totalFuelLiters(Math.round(totalFuel * 100.0) / 100.0)
                .fuelPricePerLiter(defaultFuelPrice)
                .totalFuelCostEur(Math.round(totalCost * 100.0) / 100.0)
                .loadedWeightKg(loadedWeightKg)
                .build();
    }

    public double estimateProgressiveFuel(double distanceKm, Truck truck,
                                          double initialWeightKg, double deliveredWeightKg) {
        double avgWeight = initialWeightKg - (deliveredWeightKg / 2.0);
        double loadFactor = avgWeight / truck.getMaxPayloadKg();
        double adjustedConsumption = truck.getFuelConsumptionPer100Km() * (1.0 + 0.15 * loadFactor);
        return (distanceKm / 100.0) * adjustedConsumption;
    }
}
