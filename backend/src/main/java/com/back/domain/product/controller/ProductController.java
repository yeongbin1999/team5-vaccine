package com.back.domain.product.controller;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.back.domain.product.dto.product.ProductResponseDto;
import com.back.domain.product.dto.product.ProductSearchDto;
import com.back.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService; // ProductService를 주입받습니다.

    //상품 조회 API (전체 사용자 접근 가능 - 인증 불필요)
    // 특정 상품을 조회하는 API
    @GetMapping("/api/v1/products/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Integer id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product); // 200 OK 응답
    }

    // 전체 상품을 조회하는 API
    @GetMapping("/api/v1/products")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products); // 200 OK 응답
    }

    //검색 및 필터링 API들
    // 통합 상품 검색 API
    @PostMapping("/api/v1/products/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDto>> searchProducts(@Valid @RequestBody ProductSearchDto searchDto) {
        List<ProductResponseDto> products = productService.searchProducts(searchDto);
        return ResponseEntity.ok(products);
    }
    
    // GET 방식 통합 검색 API (쿼리 파라미터 사용)
    @GetMapping("/api/v1/products/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDto>> searchProductsWithParams(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false, defaultValue = "true") Boolean includeOutOfStock,
            @RequestParam(required = false, defaultValue = "false") Boolean includeSubCategories
    ) {
        ProductSearchDto searchDto = new ProductSearchDto(
                name, categoryId, minPrice, maxPrice, minStock, 
                includeOutOfStock, includeSubCategories
        );
        List<ProductResponseDto> products = productService.searchProducts(searchDto);
        return ResponseEntity.ok(products);
    }
    
    // 상품명으로 검색하는 API
    @GetMapping("/api/v1/products/search/name")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDto>> searchProductsByName(@RequestParam String name) {
        List<ProductResponseDto> products = productService.searchByName(name);
        return ResponseEntity.ok(products);
    }
    
    // 카테고리별 상품 조회 API
    @GetMapping("/api/v1/products/category/{categoryId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeSubCategories
    ) {
        List<ProductResponseDto> products = productService.getProductsByCategory(categoryId, includeSubCategories);
        return ResponseEntity.ok(products);
    }
    
    // 가격 범위로 상품 조회 API
    @GetMapping("/api/v1/products/price-range")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDto>> getProductsByPriceRange(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice
    ) {
        List<ProductResponseDto> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    // 품절 상품 조회 API
    @GetMapping("/api/v1/products/out-of-stock")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDto>> getOutOfStockProducts() {
        List<ProductResponseDto> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
    
    // 재고 부족 상품 조회 API (관리자 전용)
    @GetMapping("/api/v1/admin/products/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold
    ) {
        List<ProductResponseDto> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    //상품 관리 API (관리자 전용)
    // 상품 생성 API
    @PostMapping("/api/v1/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED); // 201 Created 응답
    }

    // 특정 ID의 상품 수정 API
    @PutMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Integer id, 
            @Valid @RequestBody ProductRequestDto requestDto
    ) {
        ProductResponseDto updatedProduct = productService.updateProduct(id, requestDto);
        return ResponseEntity.ok(updatedProduct); // 200 OK 응답
    }

    // 특정 ID의 상품 삭제 API
    @DeleteMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }
}