package com.interhack.spring_hackaton_template.model;

import com.interhack.spring_hackaton_template.model.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    private double weightKg;

    private double widthCm;
    private double heightCm;
    private double depthCm;

    private double pricePerUnit;

    private boolean returnable;

    private int unitsPerCase;

    private int casesPerPalletLayer;
    private int maxLayersPerPallet;

    private String barcode;

    @Builder.Default
    private boolean active = true;
}
