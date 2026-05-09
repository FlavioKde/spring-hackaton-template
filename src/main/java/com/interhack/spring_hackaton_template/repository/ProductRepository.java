package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.Product;
import com.interhack.spring_hackaton_template.model.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    List<Product> findByCategory(ProductCategory category);
    List<Product> findByReturnableTrue();
}
