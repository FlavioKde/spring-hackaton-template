package com.interhack.spring_hackaton_template.controller;

import com.interhack.spring_hackaton_template.model.Order;
import com.interhack.spring_hackaton_template.model.OrderItem;
import com.interhack.spring_hackaton_template.model.enums.OrderStatus;
import com.interhack.spring_hackaton_template.repository.ClientRepository;
import com.interhack.spring_hackaton_template.repository.OrderRepository;
import com.interhack.spring_hackaton_template.repository.ProductRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    @Data
    public static class CreateOrderRequest {
        private Long clientId;
        private String deliveryDate;
        private String notes;
        private int priority;
        private List<OrderItemRequest> items;
    }

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private int quantity;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        var client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Order order = Order.builder()
                .orderCode("ORD-" + System.currentTimeMillis())
                .client(client)
                .status(OrderStatus.PENDING)
                .deliveryDate(java.time.LocalDate.parse(request.getDeliveryDate()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .priority(request.getPriority())
                .items(new ArrayList<>())
                .build();

        order = orderRepository.save(order);

        if (request.getItems() != null) {
            for (OrderItemRequest itemReq : request.getItems()) {
                var product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderItem item = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(itemReq.getQuantity())
                        .deliveredQuantity(0)
                        .returnedEmptiesQuantity(0)
                        .build();
                order.getItems().add(item);
            }
            order = orderRepository.save(order);
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found")));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Order>> getOrdersByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(orderRepository.findByClientId(clientId));
    }
}
