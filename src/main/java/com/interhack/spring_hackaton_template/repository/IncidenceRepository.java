package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.Incidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidenceRepository extends JpaRepository<Incidence, Long> {
    List<Incidence> findByRoutePlanId(Long routePlanId);
    List<Incidence> findByResolvedFalse();
    List<Incidence> findByReportedById(Long userId);
}
