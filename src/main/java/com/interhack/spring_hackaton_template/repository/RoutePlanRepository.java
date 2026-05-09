package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.RoutePlan;
import com.interhack.spring_hackaton_template.model.enums.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {
    Optional<RoutePlan> findByRouteCode(String routeCode);
    List<RoutePlan> findByDeliveryDate(LocalDate deliveryDate);
    List<RoutePlan> findByDriverId(Long driverId);
    List<RoutePlan> findByDriverIdAndStatus(Long driverId, RouteStatus status);
    List<RoutePlan> findByStatus(RouteStatus status);
}
