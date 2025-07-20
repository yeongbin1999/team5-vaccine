package com.back.domain.product.repository;

import com.back.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // 상품명으로 검색 (부분 일치, 대소문자 구분 없음)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // 카테고리 ID로 상품 조회
    List<Product> findByCategoryId(Integer categoryId);
    
    // 가격 범위로 상품 조회
    List<Product> findByPriceBetween(Integer minPrice, Integer maxPrice);
    
    // 재고가 특정 값 이상인 상품 조회
    List<Product> findByStockGreaterThanEqual(Integer minStock);
    
    // 재고가 0인 상품 조회 (품절 상품)
    List<Product> findByStock(Integer stock);
    
    // 복합 검색: 상품명 + 카테고리
    List<Product> findByNameContainingIgnoreCaseAndCategoryId(String name, Integer categoryId);
    
    // 복합 검색: 상품명 + 가격 범위
    List<Product> findByNameContainingIgnoreCaseAndPriceBetween(String name, Integer minPrice, Integer maxPrice);
    
    // 복합 검색: 카테고리 + 가격 범위
    List<Product> findByCategoryIdAndPriceBetween(Integer categoryId, Integer minPrice, Integer maxPrice);
    
    // 복합 검색: 상품명 + 카테고리 + 가격 범위
    List<Product> findByNameContainingIgnoreCaseAndCategoryIdAndPriceBetween(
            String name, Integer categoryId, Integer minPrice, Integer maxPrice);
    
    // 카테고리와 하위 카테고리의 모든 상품 조회 (JPQL 사용)
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId OR p.category.parent.id = :categoryId")
    List<Product> findByCategoryIdIncludingChildren(@Param("categoryId") Integer categoryId);
}
