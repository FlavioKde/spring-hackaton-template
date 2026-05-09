package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.LoadPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoadPlanRepository extends JpaRepository<LoadPlan, Long> {
    Optional<LoadPlan> findByRoutePlanId(Long routePlanId);
}
