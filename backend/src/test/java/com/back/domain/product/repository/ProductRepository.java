package com.back.domain.product.repository;

import com.back.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Custom query methods can be defined here if needed
}
