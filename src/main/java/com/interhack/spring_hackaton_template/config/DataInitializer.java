package com.interhack.spring_hackaton_template.config;

import com.interhack.spring_hackaton_template.model.*;
import com.interhack.spring_hackaton_template.model.enums.*;
import com.interhack.spring_hackaton_template.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final TruckRepository truckRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("Initializing sample data...");

        // Users
        User admin = userRepository.save(User.builder()
                .username("admin").password(passwordEncoder.encode("admin123"))
                .name("Administrador").email("admin@damm.es").role(UserRole.ADMIN).active(true).build());
        User packer1 = userRepository.save(User.builder()
                .username("empaquetador1").password(passwordEncoder.encode("pack123"))
                .name("Carlos Garcia").email("carlos@damm.es").role(UserRole.EMPAQUETADOR).active(true).build());
        User driver1 = userRepository.save(User.builder()
                .username("conductor1").password(passwordEncoder.encode("drive123"))
                .name("Miguel Lopez").email("miguel@damm.es").role(UserRole.CONDUCTOR).active(true).build());
        User driver2 = userRepository.save(User.builder()
                .username("conductor2").password(passwordEncoder.encode("drive123"))
                .name("Pedro Martinez").email("pedro@damm.es").role(UserRole.CONDUCTOR).active(true).build());

        // Trucks (Damm distribution fleet - Mollet del Valles depot)
        Truck truck1 = truckRepository.save(Truck.builder()
                .plate("1234-ABC").model("MAN TGL 12.250")
                .maxWeightKg(12000).cargoLengthCm(700).cargoWidthCm(240).cargoHeightCm(240)
                .fuelConsumptionPer100Km(28).tare(5500).available(true).currentDriver(driver1).build());
        Truck truck2 = truckRepository.save(Truck.builder()
                .plate("5678-DEF").model("Iveco Eurocargo 120E")
                .maxWeightKg(12000).cargoLengthCm(650).cargoWidthCm(240).cargoHeightCm(230)
                .fuelConsumptionPer100Km(30).tare(5200).available(true).currentDriver(driver2).build());

        // Products (Damm catalogue)
        Product estrella = productRepository.save(Product.builder()
                .sku("EST-33").name("Estrella Damm 33cl caja 24u")
                .category(ProductCategory.CERVEZA_BOTELLA).weightKg(9.5)
                .widthCm(40).heightCm(27).depthCm(27).pricePerUnit(12.50)
                .returnable(true).unitsPerCase(24).casesPerPalletLayer(8).maxLayersPerPallet(5)
                .barcode("8410793001012").active(true).build());
        Product voll = productRepository.save(Product.builder()
                .sku("VOL-33").name("Voll Damm 33cl caja 24u")
                .category(ProductCategory.CERVEZA_BOTELLA).weightKg(9.8)
                .widthCm(40).heightCm(27).depthCm(27).pricePerUnit(14.00)
                .returnable(true).unitsPerCase(24).casesPerPalletLayer(8).maxLayersPerPallet(5)
                .barcode("8410793002012").active(true).build());
        Product daura = productRepository.save(Product.builder()
                .sku("DAU-33").name("Daura Damm 33cl caja 24u")
                .category(ProductCategory.CERVEZA_BOTELLA).weightKg(9.3)
                .widthCm(40).heightCm(27).depthCm(27).pricePerUnit(13.50)
                .returnable(true).unitsPerCase(24).casesPerPalletLayer(8).maxLayersPerPallet(5)
                .barcode("8410793003012").active(true).build());
        Product barril30 = productRepository.save(Product.builder()
                .sku("EST-B30").name("Estrella Damm Barril 30L")
                .category(ProductCategory.CERVEZA_BARRIL).weightKg(35.0)
                .widthCm(40).heightCm(40).depthCm(40).pricePerUnit(85.00)
                .returnable(true).unitsPerCase(1).casesPerPalletLayer(4).maxLayersPerPallet(2)
                .barcode("8410793010030").active(true).build());
        Product barril50 = productRepository.save(Product.builder()
                .sku("EST-B50").name("Estrella Damm Barril 50L")
                .category(ProductCategory.CERVEZA_BARRIL).weightKg(58.0)
                .widthCm(40).heightCm(60).depthCm(40).pricePerUnit(130.00)
                .returnable(true).unitsPerCase(1).casesPerPalletLayer(4).maxLayersPerPallet(1)
                .barcode("8410793010050").active(true).build());
        Product lataEstrella = productRepository.save(Product.builder()
                .sku("EST-L33").name("Estrella Damm Lata 33cl pack 24u")
                .category(ProductCategory.CERVEZA_LATA).weightKg(8.5)
                .widthCm(40).heightCm(15).depthCm(27).pricePerUnit(11.00)
                .returnable(false).unitsPerCase(24).casesPerPalletLayer(10).maxLayersPerPallet(6)
                .barcode("8410793020012").active(true).build());
        Product agua = productRepository.save(Product.builder()
                .sku("FNT-150").name("Font Vella 1.5L pack 6u")
                .category(ProductCategory.AGUA).weightKg(9.5)
                .widthCm(35).heightCm(32).depthCm(23).pricePerUnit(4.50)
                .returnable(false).unitsPerCase(6).casesPerPalletLayer(10).maxLayersPerPallet(4)
                .barcode("8410793030015").active(true).build());

        // Clients (Barcelona area bars/restaurants)
        Client c1 = clientRepository.save(Client.builder()
                .name("Bar El Raval").address("C/ Hospital 74, Barcelona")
                .city("Barcelona").postalCode("08001").contactPhone("934567890")
                .latitude(41.3797).longitude(2.1682).zone("Barcelona-Centro")
                .deliveryWindow("08:00-14:00").active(true).build());
        Client c2 = clientRepository.save(Client.builder()
                .name("Restaurant Can Paixano").address("C/ Reina Cristina 7, Barcelona")
                .city("Barcelona").postalCode("08003").contactPhone("933101839")
                .latitude(41.3776).longitude(2.1856).zone("Barcelona-Centro")
                .deliveryWindow("07:00-12:00").active(true).build());
        Client c3 = clientRepository.save(Client.builder()
                .name("Cerveceria Catalana").address("C/ Mallorca 236, Barcelona")
                .city("Barcelona").postalCode("08008").contactPhone("932160368")
                .latitude(41.3940).longitude(2.1610).zone("Barcelona-Eixample")
                .deliveryWindow("08:00-15:00").active(true).build());
        Client c4 = clientRepository.save(Client.builder()
                .name("Bar Mut").address("C/ Pau Claris 192, Barcelona")
                .city("Barcelona").postalCode("08037").contactPhone("932174338")
                .latitude(41.3974).longitude(2.1629).zone("Barcelona-Eixample")
                .deliveryWindow("09:00-14:00").active(true).build());
        Client c5 = clientRepository.save(Client.builder()
                .name("La Bodegueta de Gracia").address("C/ Gran de Gracia 47, Barcelona")
                .city("Barcelona").postalCode("08012").contactPhone("932184598")
                .latitude(41.4012).longitude(2.1573).zone("Barcelona-Gracia")
                .deliveryWindow("08:00-13:00").active(true).build());
        Client c6 = clientRepository.save(Client.builder()
                .name("Bar Leo").address("C/ Sant Carles 34, Barcelona")
                .city("Barcelona").postalCode("08003").contactPhone("933105516")
                .latitude(41.3802).longitude(2.1893).zone("Barcelona-Barceloneta")
                .deliveryWindow("07:00-11:00").active(true).build());
        Client c7 = clientRepository.save(Client.builder()
                .name("Super Bar Mollet").address("C/ Gaietà Vínzia 15, Mollet del Vallès")
                .city("Mollet del Vallès").postalCode("08100").contactPhone("935703456")
                .latitude(41.5388).longitude(2.1907).zone("Mollet")
                .deliveryWindow("08:00-16:00").active(true).build());
        Client c8 = clientRepository.save(Client.builder()
                .name("Restaurant El Celler Mollet").address("Av. Llibertat 40, Mollet del Vallès")
                .city("Mollet del Vallès").postalCode("08100").contactPhone("935793210")
                .latitude(41.5410).longitude(2.1875).zone("Mollet")
                .deliveryWindow("09:00-15:00").active(true).build());

        // Sample orders for today
        LocalDate today = LocalDate.now();

        createOrder("ORD-001", c1, today, List.of(
                new ItemQty(estrella, 10), new ItemQty(barril30, 2), new ItemQty(agua, 5)));
        createOrder("ORD-002", c2, today, List.of(
                new ItemQty(estrella, 15), new ItemQty(voll, 5), new ItemQty(barril50, 1)));
        createOrder("ORD-003", c3, today, List.of(
                new ItemQty(estrella, 8), new ItemQty(daura, 3), new ItemQty(lataEstrella, 10)));
        createOrder("ORD-004", c4, today, List.of(
                new ItemQty(voll, 10), new ItemQty(barril30, 3)));
        createOrder("ORD-005", c5, today, List.of(
                new ItemQty(estrella, 20), new ItemQty(agua, 10)));
        createOrder("ORD-006", c6, today, List.of(
                new ItemQty(estrella, 12), new ItemQty(barril50, 2), new ItemQty(lataEstrella, 5)));
        createOrder("ORD-007", c7, today, List.of(
                new ItemQty(estrella, 30), new ItemQty(voll, 10), new ItemQty(barril30, 5)));
        createOrder("ORD-008", c8, today, List.of(
                new ItemQty(daura, 8), new ItemQty(agua, 15), new ItemQty(lataEstrella, 12)));

        log.info("Sample data initialized: {} users, {} clients, {} products, {} trucks, {} orders",
                userRepository.count(), clientRepository.count(), productRepository.count(),
                truckRepository.count(), orderRepository.count());
    }

    private void createOrder(String code, Client client, LocalDate date, List<ItemQty> items) {
        Order order = Order.builder()
                .orderCode(code)
                .client(client)
                .status(OrderStatus.PENDING)
                .deliveryDate(date)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .priority(1)
                .items(new ArrayList<>())
                .build();
        order = orderRepository.save(order);

        for (ItemQty iq : items) {
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(iq.product)
                    .quantity(iq.quantity)
                    .deliveredQuantity(0)
                    .returnedEmptiesQuantity(0)
                    .build();
            order.getItems().add(item);
        }
        orderRepository.save(order);
    }

    private record ItemQty(Product product, int quantity) {}
}
