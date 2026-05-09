package com.interhack.spring_hackaton_template.model;

import com.interhack.spring_hackaton_template.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDate deliveryDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String notes;

    private int priority;

    public double getTotalWeight() {
        return items.stream()
                .mapToDouble(i -> i.getQuantity() * i.getProduct().getWeightKg())
                .sum();
    }

    public double getTotalVolume() {
        return items.stream()
                .mapToDouble(i -> i.getQuantity()
                        * i.getProduct().getWidthCm()
                        * i.getProduct().getHeightCm()
                        * i.getProduct().getDepthCm())
                .sum();
    }

    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(i -> i.getQuantity() * i.getProduct().getPricePerUnit())
                .sum();
    }
}
