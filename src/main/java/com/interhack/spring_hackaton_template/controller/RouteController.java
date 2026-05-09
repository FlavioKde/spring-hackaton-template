package com.interhack.spring_hackaton_template.controller;

import com.interhack.spring_hackaton_template.dto.RouteOptimizationRequest;
import com.interhack.spring_hackaton_template.dto.RouteOptimizationResponse;
import com.interhack.spring_hackaton_template.model.RoutePlan;
import com.interhack.spring_hackaton_template.service.RecomendationService;
import com.interhack.spring_hackaton_template.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RecomendationService recomendationService;

    @PostMapping("/optimize")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RouteOptimizationResponse> optimizeRoute(
            @RequestBody RouteOptimizationRequest request) {
        return ResponseEntity.ok(routeService.optimizeRoute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutePlan> getRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRoutePlan(id));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<RoutePlan>> getRoutesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(routeService.getRoutesByDate(date));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RoutePlan>> getRoutesByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(routeService.getRoutesByDriver(driverId));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<RoutePlan> startRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.startRoute(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<RoutePlan> completeRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.completeRoute(id));
    }

    @GetMapping("/recommendations/{date}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<RecomendationService.RouteRecommendation>> getRecommendations(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(recomendationService.recommendRoutes(date));
    }
}
