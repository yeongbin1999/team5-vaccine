package com.back.domain.product.controller;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.back.domain.product.dto.product.ProductResponseDto;
import com.back.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService; // ProductService를 주입받습니다.

    //상품 조회 API (전체 사용자 접근 가능)
    // 특정 상품을 조회하는 API
    @GetMapping("/api/v1/products/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Integer id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product); // 200 OK 응답
    }

    // 전체 상품을 조회하는 API
    @GetMapping("/api/v1/products")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products); // 200 OK 응답
    }

    //상품 관리 API (관리자 전용)
    // 상품 생성 API
    @PostMapping("/api/v1/admin/products")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED); // 201 Created 응답
    }

    // 특정 ID의 상품 수정 API
    @PutMapping("/api/v1/admin/products/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto updatedProduct = productService.updateProduct(id, requestDto);
        return ResponseEntity.ok(updatedProduct); // 200 OK 응답
    }

    // 특정 ID의 상품 삭제 API
    @DeleteMapping("/api/v1/admin/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }
}