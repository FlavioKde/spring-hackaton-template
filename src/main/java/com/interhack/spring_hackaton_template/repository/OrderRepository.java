package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.Order;
import com.interhack.spring_hackaton_template.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByDeliveryDate(LocalDate deliveryDate);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByDeliveryDateAndStatus(LocalDate deliveryDate, OrderStatus status);
    List<Order> findByClientId(Long clientId);
}
