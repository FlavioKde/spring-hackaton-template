package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck, Long> {
    Optional<Truck> findByPlate(String plate);
    List<Truck> findByAvailableTrue();
    Optional<Truck> findByCurrentDriverId(Long driverId);
}
