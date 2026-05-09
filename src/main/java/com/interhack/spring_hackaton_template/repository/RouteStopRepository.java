package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findByRoutePlanIdOrderBySequenceAsc(Long routePlanId);
}
