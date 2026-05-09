package com.interhack.spring_hackaton_template.controller;

import com.interhack.spring_hackaton_template.dto.DeliveryConfirmationRequest;
import com.interhack.spring_hackaton_template.dto.DriverRouteDTO;
import com.interhack.spring_hackaton_template.model.DeliveryNote;
import com.interhack.spring_hackaton_template.service.DeliveryService;
import com.interhack.spring_hackaton_template.service.ReturnablesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DeliveryService deliveryService;
    private final ReturnablesService returnablesService;

    @GetMapping("/route/{driverId}")
    public ResponseEntity<DriverRouteDTO> getMyRoute(@PathVariable Long driverId) {
        return ResponseEntity.ok(deliveryService.getDriverRoute(driverId));
    }

    @PostMapping("/deliver/{driverId}")
    public ResponseEntity<DeliveryNote> confirmDelivery(
            @PathVariable Long driverId,
            @RequestBody DeliveryConfirmationRequest request) {
        return ResponseEntity.ok(deliveryService.confirmDelivery(driverId, request));
    }

    @GetMapping("/returnables/{routePlanId}")
    public ResponseEntity<ReturnablesService.ReturnablesSummary> getReturnables(
            @PathVariable Long routePlanId) {
        return ResponseEntity.ok(returnablesService.getReturnablesSummary(routePlanId));
    }
}
