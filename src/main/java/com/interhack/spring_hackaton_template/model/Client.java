package com.interhack.spring_hackaton_template.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String city;
    private String postalCode;
    private String contactPhone;
    private String contactEmail;

    private double latitude;
    private double longitude;

    private String zone;

    private String deliveryWindow;

    private String notes;

    @Builder.Default
    private boolean active = true;
}
