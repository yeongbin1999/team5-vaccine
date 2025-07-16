package com.back.domain.product.repository;

import com.back.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Custom query methods can be defined here if needed
}
