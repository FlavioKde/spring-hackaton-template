package com.interhack.spring_hackaton_template.controller;

import com.interhack.spring_hackaton_template.dto.DashboardDTO;
import com.interhack.spring_hackaton_template.model.*;
import com.interhack.spring_hackaton_template.model.enums.OrderStatus;
import com.interhack.spring_hackaton_template.model.enums.RouteStatus;
import com.interhack.spring_hackaton_template.model.enums.UserRole;
import com.interhack.spring_hackaton_template.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final TruckRepository truckRepository;
    private final RoutePlanRepository routePlanRepository;
    private final IncidenceRepository incidenceRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        LocalDate today = LocalDate.now();
        List<RoutePlan> todayRoutes = routePlanRepository.findByDeliveryDate(today);

        DashboardDTO dashboard = DashboardDTO.builder()
                .totalRoutesToday(todayRoutes.size())
                .completedRoutes((int) todayRoutes.stream()
                        .filter(r -> r.getStatus() == RouteStatus.COMPLETED).count())
                .activeRoutes((int) todayRoutes.stream()
                        .filter(r -> r.getStatus() == RouteStatus.IN_PROGRESS).count())
                .pendingOrders(orderRepository.findByStatus(OrderStatus.PENDING).size())
                .totalDeliveries((int) todayRoutes.stream()
                        .mapToLong(RoutePlan::getCompletedStops).sum())
                .incidences(incidenceRepository.findByResolvedFalse().size())
                .totalFuelCostToday(todayRoutes.stream()
                        .mapToDouble(RoutePlan::getTotalFuelCostEur).sum())
                .build();

        return ResponseEntity.ok(dashboard);
    }

    // --- Users ---
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userRepository.findByRole(role));
    }

    // --- Clients ---
    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getClients() {
        return ResponseEntity.ok(clientRepository.findByActiveTrue());
    }

    @PostMapping("/clients")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientRepository.save(client));
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client client) {
        client.setId(id);
        return ResponseEntity.ok(clientRepository.save(client));
    }

    // --- Products ---
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productRepository.save(product));
    }

    // --- Trucks ---
    @GetMapping("/trucks")
    public ResponseEntity<List<Truck>> getTrucks() {
        return ResponseEntity.ok(truckRepository.findAll());
    }

    @PostMapping("/trucks")
    public ResponseEntity<Truck> createTruck(@RequestBody Truck truck) {
        return ResponseEntity.ok(truckRepository.save(truck));
    }

    // --- Orders ---
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/orders/date/{date}")
    public ResponseEntity<List<Order>> getOrdersByDate(@PathVariable LocalDate date) {
        return ResponseEntity.ok(orderRepository.findByDeliveryDate(date));
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderRepository.findByStatus(status));
    }

    // --- Incidences ---
    @GetMapping("/incidences")
    public ResponseEntity<List<Incidence>> getPendingIncidences() {
        return ResponseEntity.ok(incidenceRepository.findByResolvedFalse());
    }

    @PostMapping("/incidences/{id}/resolve")
    public ResponseEntity<Incidence> resolveIncidence(@PathVariable Long id, @RequestBody String resolution) {
        Incidence incidence = incidenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidence not found"));
        incidence.setResolved(true);
        incidence.setResolution(resolution);
        incidence.setResolvedAt(java.time.LocalDateTime.now());
        return ResponseEntity.ok(incidenceRepository.save(incidence));
    }
}
