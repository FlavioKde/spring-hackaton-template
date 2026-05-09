package com.interhack.spring_hackaton_template.controller;

import com.interhack.spring_hackaton_template.dto.FuelCostDTO;
import com.interhack.spring_hackaton_template.model.Truck;
import com.interhack.spring_hackaton_template.repository.TruckRepository;
import com.interhack.spring_hackaton_template.service.FuelService;
import com.interhack.spring_hackaton_template.service.GoogleMapsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final GoogleMapsService googleMapsService;
    private final FuelService fuelService;
    private final TruckRepository truckRepository;

    @GetMapping("/distance")
    public ResponseEntity<GoogleMapsService.DistanceResult> getDistance(
            @RequestParam double originLat, @RequestParam double originLng,
            @RequestParam double destLat, @RequestParam double destLng) {
        return ResponseEntity.ok(googleMapsService.getDistance(originLat, originLng, destLat, destLng));
    }

    @GetMapping("/fuel-cost")
    public ResponseEntity<FuelCostDTO> getFuelCost(
            @RequestParam double distanceKm,
            @RequestParam Long truckId,
            @RequestParam double loadedWeightKg) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));
        return ResponseEntity.ok(fuelService.calculateFuelCost(distanceKm, truck, loadedWeightKg));
    }
}
