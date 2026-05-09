package com.interhack.spring_hackaton_template.model;

import com.interhack.spring_hackaton_template.model.enums.DeliveryStatus;
import com.interhack.spring_hackaton_template.model.enums.IncidenceType;
import com.interhack.spring_hackaton_template.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String albaranCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_stop_id")
    private RouteStop routeStop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private double totalAmount;
    private double collectedAmount;

    private int totalReturnablesCollected;

    private LocalDateTime deliveredAt;

    private String clientSignature;

    @Enumerated(EnumType.STRING)
    private IncidenceType incidenceType;
    private String incidenceDescription;

    private String photoUrl;

    private String notes;
}
