package com.interhack.spring_hackaton_template.service;

import com.interhack.spring_hackaton_template.model.*;
import com.interhack.spring_hackaton_template.repository.LoadPlanRepository;
import com.interhack.spring_hackaton_template.repository.PalletRepository;
import com.interhack.spring_hackaton_template.repository.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadService {

    private final LoadPlanRepository loadPlanRepository;
    private final PalletRepository palletRepository;
    private final RouteStopRepository routeStopRepository;

    @Value("${pallet.default.width-cm}")
    private double palletWidthCm;
    @Value("${pallet.default.length-cm}")
    private double palletLengthCm;
    @Value("${pallet.default.max-height-cm}")
    private double palletMaxHeightCm;
    @Value("${pallet.default.max-weight-kg}")
    private double palletMaxWeightKg;

    @Transactional
    public LoadPlan generateLoadPlan(RoutePlan routePlan, List<Order> orders, Truck truck) {
        LoadPlan loadPlan = LoadPlan.builder()
                .routePlan(routePlan)
                .totalWeightKg(0)
                .totalPallets(0)
                .safeDistribution(false)
                .build();
        loadPlan = loadPlanRepository.save(loadPlan);

        List<RouteStop> stops = routeStopRepository.findByRoutePlanIdOrderBySequenceAsc(routePlan.getId());

        // Hybrid loading: group items by stop, then pack onto pallets
        // Last stops go to back of truck (loaded first, unloaded last)
        // First stops go to front/door side (loaded last, unloaded first)
        List<OrderItemGroup> allItems = new ArrayList<>();
        for (RouteStop stop : stops) {
            for (Order order : stop.getOrders()) {
                for (OrderItem item : order.getItems()) {
                    allItems.add(new OrderItemGroup(item, stop.getSequence(), order));
                }
            }
        }

        // Sort by stop sequence DESCENDING (last delivery first into truck = back)
        allItems.sort(Comparator.comparingInt(OrderItemGroup::stopSequence).reversed());

        List<Pallet> pallets = new ArrayList<>();
        int palletNum = 1;

        // First Fit Decreasing bin packing
        for (OrderItemGroup itemGroup : allItems) {
            OrderItem item = itemGroup.item;
            double itemWeight = item.getQuantity() * item.getProduct().getWeightKg();
            double itemHeight = item.getProduct().getHeightCm() *
                    Math.ceil((double) item.getQuantity() / Math.max(1, item.getProduct().getCasesPerPalletLayer()));

            Pallet targetPallet = null;
            for (Pallet p : pallets) {
                if (p.getTargetStopSequence() == itemGroup.stopSequence
                        && p.getTotalWeightKg() + itemWeight <= palletMaxWeightKg
                        && p.getCurrentHeightCm() + itemHeight <= palletMaxHeightCm) {
                    targetPallet = p;
                    break;
                }
            }

            if (targetPallet == null) {
                targetPallet = Pallet.builder()
                        .loadPlan(loadPlan)
                        .palletNumber(palletNum++)
                        .widthCm(palletWidthCm)
                        .lengthCm(palletLengthCm)
                        .currentHeightCm(0)
                        .maxHeightCm(palletMaxHeightCm)
                        .totalWeightKg(0)
                        .maxWeightKg(palletMaxWeightKg)
                        .targetStopSequence(itemGroup.stopSequence)
                        .loaded(false)
                        .unloaded(false)
                        .qrCode("PLT-" + routePlan.getRouteCode() + "-" + (palletNum - 1))
                        .build();
                pallets.add(targetPallet);
            }

            int layer = (int) Math.ceil(targetPallet.getCurrentHeightCm() / 30.0) + 1;
            PalletItem palletItem = PalletItem.builder()
                    .pallet(targetPallet)
                    .product(item.getProduct())
                    .order(itemGroup.order)
                    .quantity(item.getQuantity())
                    .layerNumber(layer)
                    .positionInLayer(1)
                    .weightKg(itemWeight)
                    .build();

            targetPallet.getItems().add(palletItem);
            targetPallet.setTotalWeightKg(targetPallet.getTotalWeightKg() + itemWeight);
            targetPallet.setCurrentHeightCm(targetPallet.getCurrentHeightCm() + itemHeight);
        }

        // Position pallets in truck for weight distribution
        positionPalletsInTruck(pallets, truck);

        // Calculate center of gravity
        double totalWeight = 0;
        double weightedX = 0;
        double weightedY = 0;

        for (Pallet p : pallets) {
            palletRepository.save(p);
            totalWeight += p.getTotalWeightKg();
            weightedX += p.getPositionXCm() * p.getTotalWeightKg();
            weightedY += p.getPositionYCm() * p.getTotalWeightKg();
        }

        double cogX = totalWeight > 0 ? weightedX / totalWeight : truck.getCargoLengthCm() / 2;
        double cogY = totalWeight > 0 ? weightedY / totalWeight : truck.getCargoWidthCm() / 2;

        // Weight balance check (golpe de ariete prevention)
        double truckCenterX = truck.getCargoLengthCm() / 2;
        double truckCenterY = truck.getCargoWidthCm() / 2;

        double frontWeight = 0, rearWeight = 0, leftWeight = 0, rightWeight = 0;
        for (Pallet p : pallets) {
            if (p.getPositionXCm() < truckCenterX) {
                frontWeight += p.getTotalWeightKg();
            } else {
                rearWeight += p.getTotalWeightKg();
            }
            if (p.getPositionYCm() < truckCenterY) {
                leftWeight += p.getTotalWeightKg();
            } else {
                rightWeight += p.getTotalWeightKg();
            }
        }

        double frontPercent = totalWeight > 0 ? frontWeight / totalWeight * 100 : 50;
        double rearPercent = totalWeight > 0 ? rearWeight / totalWeight * 100 : 50;
        double leftPercent = totalWeight > 0 ? leftWeight / totalWeight * 100 : 50;
        double rightPercent = totalWeight > 0 ? rightWeight / totalWeight * 100 : 50;

        // Safe if no more than 60/40 imbalance in any axis
        boolean safe = Math.abs(frontPercent - 50) <= 10 && Math.abs(leftPercent - 50) <= 10;

        loadPlan.setTotalWeightKg(totalWeight);
        loadPlan.setTotalPallets(pallets.size());
        loadPlan.setCenterOfGravityX(Math.round(cogX * 100.0) / 100.0);
        loadPlan.setCenterOfGravityY(Math.round(cogY * 100.0) / 100.0);
        loadPlan.setWeightBalanceFrontPercent(Math.round(frontPercent * 100.0) / 100.0);
        loadPlan.setWeightBalanceRearPercent(Math.round(rearPercent * 100.0) / 100.0);
        loadPlan.setWeightBalanceLeftPercent(Math.round(leftPercent * 100.0) / 100.0);
        loadPlan.setWeightBalanceRightPercent(Math.round(rightPercent * 100.0) / 100.0);
        loadPlan.setSafeDistribution(safe);

        StringBuilder instructions = new StringBuilder();
        instructions.append("LOADING ORDER (load first = unload last):\n");
        List<Pallet> sortedByStop = new ArrayList<>(pallets);
        sortedByStop.sort(Comparator.comparingInt(Pallet::getTargetStopSequence).reversed());
        for (Pallet p : sortedByStop) {
            instructions.append(String.format("  Pallet #%d [%s] -> Stop %d | Pos: (%.0f, %.0f) | %.1f kg\n",
                    p.getPalletNumber(), p.getQrCode(), p.getTargetStopSequence(),
                    p.getPositionXCm(), p.getPositionYCm(), p.getTotalWeightKg()));
        }
        if (!safe) {
            instructions.append("\n*** WARNING: Weight distribution is unbalanced! Risk of golpe de ariete. ***\n");
        }
        loadPlan.setLoadingInstructions(instructions.toString());

        return loadPlanRepository.save(loadPlan);
    }

    private void positionPalletsInTruck(List<Pallet> pallets, Truck truck) {
        double truckLength = truck.getCargoLengthCm();
        double truckWidth = truck.getCargoWidthCm();

        int palletsPerRow = (int) (truckWidth / palletWidthCm);
        if (palletsPerRow < 1) palletsPerRow = 1;

        // Sort: last stops first (they go deepest into the truck)
        pallets.sort(Comparator.comparingInt(Pallet::getTargetStopSequence).reversed());

        double currentX = 0;
        int col = 0;

        // Alternate heavy/light pallets left/right for balance
        List<Pallet> sortedByWeight = new ArrayList<>(pallets);
        sortedByWeight.sort(Comparator.comparingDouble(Pallet::getTotalWeightKg).reversed());

        Map<Pallet, double[]> positions = new LinkedHashMap<>();
        double x = 0;
        int c = 0;

        for (int i = 0; i < pallets.size(); i++) {
            Pallet p = pallets.get(i);
            double posY;
            if (palletsPerRow >= 2) {
                posY = (c % 2 == 0) ? palletWidthCm / 2 : truckWidth - palletWidthCm / 2;
            } else {
                posY = truckWidth / 2;
            }

            p.setPositionXCm(x + palletLengthCm / 2);
            p.setPositionYCm(posY);

            c++;
            if (c >= palletsPerRow) {
                c = 0;
                x += palletLengthCm;
            }
        }

        // Rebalance: swap pallets between left/right to minimize lateral imbalance
        for (int iter = 0; iter < 20; iter++) {
            double leftW = 0, rightW = 0;
            double center = truckWidth / 2;
            List<Pallet> leftPallets = new ArrayList<>();
            List<Pallet> rightPallets = new ArrayList<>();

            for (Pallet p : pallets) {
                if (p.getPositionYCm() < center) {
                    leftW += p.getTotalWeightKg();
                    leftPallets.add(p);
                } else {
                    rightW += p.getTotalWeightKg();
                    rightPallets.add(p);
                }
            }

            if (Math.abs(leftW - rightW) < leftW * 0.05) break;

            // Find best swap
            double bestImprovement = 0;
            Pallet bestLeft = null, bestRight = null;

            for (Pallet lp : leftPallets) {
                for (Pallet rp : rightPallets) {
                    if (lp.getTargetStopSequence() != rp.getTargetStopSequence()) continue;
                    double newLeftW = leftW - lp.getTotalWeightKg() + rp.getTotalWeightKg();
                    double newRightW = rightW - rp.getTotalWeightKg() + lp.getTotalWeightKg();
                    double improvement = Math.abs(leftW - rightW) - Math.abs(newLeftW - newRightW);
                    if (improvement > bestImprovement) {
                        bestImprovement = improvement;
                        bestLeft = lp;
                        bestRight = rp;
                    }
                }
            }

            if (bestLeft != null) {
                double tmpY = bestLeft.getPositionYCm();
                bestLeft.setPositionYCm(bestRight.getPositionYCm());
                bestRight.setPositionYCm(tmpY);
            } else {
                break;
            }
        }
    }

    public LoadPlan getLoadPlan(Long routePlanId) {
        return loadPlanRepository.findByRoutePlanId(routePlanId)
                .orElseThrow(() -> new RuntimeException("Load plan not found for route " + routePlanId));
    }

    private record OrderItemGroup(OrderItem item, int stopSequence, Order order) {}
}
