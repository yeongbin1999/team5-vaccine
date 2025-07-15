package com.back.domain.product.repository;

import com.back.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // 부모 카테고리가 없는(null) 모든 카테고리를 조회하는 메서드
    List<Category> findByParentIsNull();
}