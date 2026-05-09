package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long> {
    Optional<DeliveryNote> findByAlbaranCode(String albaranCode);
    List<DeliveryNote> findByDriverId(Long driverId);
    List<DeliveryNote> findByRouteStopId(Long routeStopId);
    List<DeliveryNote> findByOrderId(Long orderId);
}
