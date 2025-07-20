package com.back.domain.admin.controller;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.back.domain.product.dto.product.ProductResponseDto;
import com.back.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    // 상품 생성 API
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // 특정 ID의 상품 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Integer id, 
            @Valid @RequestBody ProductRequestDto requestDto
    ) {
        ProductResponseDto updatedProduct = productService.updateProduct(id, requestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    // 특정 ID의 상품 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // 재고 부족 상품 조회 API
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponseDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold
    ) {
        List<ProductResponseDto> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
}
