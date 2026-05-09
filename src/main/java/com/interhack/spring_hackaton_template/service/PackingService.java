package com.interhack.spring_hackaton_template.service;

import com.interhack.spring_hackaton_template.dto.PackingTaskDTO;
import com.interhack.spring_hackaton_template.model.*;
import com.interhack.spring_hackaton_template.model.enums.RouteStatus;
import com.interhack.spring_hackaton_template.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackingService {

    private final RoutePlanRepository routePlanRepository;
    private final LoadPlanRepository loadPlanRepository;
    private final PalletRepository palletRepository;
    private final RouteStopRepository routeStopRepository;

    public List<PackingTaskDTO> getPendingPackingTasks() {
        List<RoutePlan> routes = routePlanRepository.findByStatus(RouteStatus.OPTIMIZED);
        routes.addAll(routePlanRepository.findByStatus(RouteStatus.LOADING));

        List<PackingTaskDTO> tasks = new ArrayList<>();
        for (RoutePlan route : routes) {
            tasks.add(buildPackingTask(route));
        }
        return tasks;
    }

    public PackingTaskDTO getPackingTask(Long routePlanId) {
        RoutePlan route = routePlanRepository.findById(routePlanId)
                .orElseThrow(() -> new RuntimeException("Route plan not found"));
        return buildPackingTask(route);
    }

    private PackingTaskDTO buildPackingTask(RoutePlan route) {
        LoadPlan loadPlan = loadPlanRepository.findByRoutePlanId(route.getId()).orElse(null);
        if (loadPlan == null) {
            return PackingTaskDTO.builder()
                    .routePlanId(route.getId())
                    .routeCode(route.getRouteCode())
                    .totalPallets(0)
                    .packedPallets(0)
                    .pallets(List.of())
                    .build();
        }

        List<Pallet> pallets = palletRepository.findByLoadPlanIdOrderByPalletNumberAsc(loadPlan.getId());
        List<RouteStop> stops = routeStopRepository.findByRoutePlanIdOrderBySequenceAsc(route.getId());

        List<PackingTaskDTO.PalletTask> palletTasks = new ArrayList<>();
        for (Pallet pallet : pallets) {
            String targetClient = stops.stream()
                    .filter(s -> s.getSequence() == pallet.getTargetStopSequence())
                    .findFirst()
                    .map(s -> s.getClient().getName())
                    .orElse("Unknown");

            List<PackingTaskDTO.ItemToPick> items = pallet.getItems().stream()
                    .map(pi -> PackingTaskDTO.ItemToPick.builder()
                            .productId(pi.getProduct().getId())
                            .productName(pi.getProduct().getName())
                            .sku(pi.getProduct().getSku())
                            .barcode(pi.getProduct().getBarcode())
                            .quantity(pi.getQuantity())
                            .warehouseLocation("A-" + pi.getProduct().getId())
                            .picked(false)
                            .build())
                    .toList();

            palletTasks.add(PackingTaskDTO.PalletTask.builder()
                    .palletId(pallet.getId())
                    .palletNumber(pallet.getPalletNumber())
                    .qrCode(pallet.getQrCode())
                    .loaded(pallet.isLoaded())
                    .targetStopSequence(pallet.getTargetStopSequence())
                    .targetClientName(targetClient)
                    .itemsToPick(items)
                    .totalWeightKg(pallet.getTotalWeightKg())
                    .build());
        }

        int packedCount = (int) pallets.stream().filter(Pallet::isLoaded).count();

        return PackingTaskDTO.builder()
                .routePlanId(route.getId())
                .routeCode(route.getRouteCode())
                .totalPallets(pallets.size())
                .packedPallets(packedCount)
                .pallets(palletTasks)
                .build();
    }

    @Transactional
    public void markPalletLoaded(Long palletId) {
        Pallet pallet = palletRepository.findById(palletId)
                .orElseThrow(() -> new RuntimeException("Pallet not found"));
        pallet.setLoaded(true);
        palletRepository.save(pallet);

        LoadPlan loadPlan = pallet.getLoadPlan();
        List<Pallet> allPallets = palletRepository.findByLoadPlanIdOrderByPalletNumberAsc(loadPlan.getId());
        boolean allLoaded = allPallets.stream().allMatch(Pallet::isLoaded);

        if (allLoaded) {
            RoutePlan route = loadPlan.getRoutePlan();
            route.setStatus(RouteStatus.READY);
            routePlanRepository.save(route);
        } else {
            RoutePlan route = loadPlan.getRoutePlan();
            if (route.getStatus() == RouteStatus.OPTIMIZED) {
                route.setStatus(RouteStatus.LOADING);
                routePlanRepository.save(route);
            }
        }
    }

    @Transactional
    public void markPalletUnloaded(Long palletId) {
        Pallet pallet = palletRepository.findById(palletId)
                .orElseThrow(() -> new RuntimeException("Pallet not found"));
        pallet.setUnloaded(true);
        palletRepository.save(pallet);
    }
}
