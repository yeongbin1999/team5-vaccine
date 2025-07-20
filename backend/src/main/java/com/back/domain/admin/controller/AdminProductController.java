package com.back.domain.admin.controller;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.back.domain.product.dto.product.ProductResponseDto;
import com.back.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "관리자 - 상품 생성",
            description = "새로운 상품을 생성합니다. 관리자 권한이 필요합니다.")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // 특정 ID의 상품 수정 API
    @PutMapping("/{id}")
    @Operation(summary = "관리자 - 상품 정보 수정",
            description = "특정 상품 ID의 정보를 수정합니다. 관리자 권한이 필요합니다.")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Integer id, 
            @Valid @RequestBody ProductRequestDto requestDto
    ) {
        ProductResponseDto updatedProduct = productService.updateProduct(id, requestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    // 특정 ID의 상품 삭제 API
    @DeleteMapping("/{id}")
    @Operation(summary = "관리자 - 상품 삭제",
            description = "특정 상품을 삭제합니다. 관리자 권한이 필요합니다.")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // 재고 부족 상품 조회 API
    @GetMapping("/low-stock")
    @Operation(summary = "관리자 - 재고 부족 상품 조회",
            description = "지정된 임계값 이하의 재고를 가진 상품들을 조회합니다. 현재 기본 임계값은 10개입니다.")
    public ResponseEntity<List<ProductResponseDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold
    ) {
        List<ProductResponseDto> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
}
