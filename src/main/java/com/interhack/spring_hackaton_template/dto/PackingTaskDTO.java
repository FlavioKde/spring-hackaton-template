package com.interhack.spring_hackaton_template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class PackingTaskDTO {
    private Long routePlanId;
    private String routeCode;
    private int totalPallets;
    private int packedPallets;
    private List<PalletTask> pallets;

    @Data
    @AllArgsConstructor
    @Builder
    public static class PalletTask {
        private Long palletId;
        private int palletNumber;
        private String qrCode;
        private boolean loaded;
        private int targetStopSequence;
        private String targetClientName;
        private List<ItemToPick> itemsToPick;
        private double totalWeightKg;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class ItemToPick {
        private Long productId;
        private String productName;
        private String sku;
        private String barcode;
        private int quantity;
        private String warehouseLocation;
        private boolean picked;
    }
}
