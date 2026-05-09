package com.interhack.spring_hackaton_template.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pallet_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PalletItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pallet_id")
    private Pallet pallet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private int quantity;

    private int layerNumber;
    private int positionInLayer;

    private double weightKg;
}
