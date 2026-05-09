package com.interhack.spring_hackaton_template.dto;

import com.interhack.spring_hackaton_template.model.enums.IncidenceType;
import com.interhack.spring_hackaton_template.model.enums.PaymentMethod;
import lombok.Data;

import java.util.List;

@Data
public class DeliveryConfirmationRequest {
    private Long routeStopId;
    private Long orderId;
    private List<DeliveredItem> deliveredItems;
    private List<ReturnedItem> returnedItems;
    private PaymentMethod paymentMethod;
    private double collectedAmount;
    private String clientSignature;
    private String notes;
    private boolean hasIncidence;
    private IncidenceType incidenceType;
    private String incidenceDescription;

    @Data
    public static class DeliveredItem {
        private Long productId;
        private int quantity;
    }

    @Data
    public static class ReturnedItem {
        private Long productId;
        private int quantity;
    }
}
