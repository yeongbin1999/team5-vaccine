package com.back.domain.product.controller;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.back.domain.product.dto.product.ProductResponseDto;
import com.back.domain.product.dto.product.ProductSearchDto;
import com.back.domain.product.service.ProductService;
import com.back.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid; // DTO 유효성 검증을 위한 어노테이션
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // HTTP 상태 코드
import org.springframework.http.ResponseEntity; // HTTP 응답 객체
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*; // Spring Web 어노테이션들

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService; // ProductService를 주입받습니다.

    //상품 조회 API (전체 사용자 접근 가능 - 인증 불필요)
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

    //검색 및 필터링 API들
    // 통합 상품 검색 API
    @PostMapping("/api/v1/products/search")
    public ResponseEntity<List<ProductResponseDto>> searchProducts(@Valid @RequestBody ProductSearchDto searchDto) {
        List<ProductResponseDto> products = productService.searchProducts(searchDto);
        return ResponseEntity.ok(products);
    }
    
    // GET 방식 통합 검색 API (쿼리 파라미터 사용)
    @GetMapping("/api/v1/products/search")
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
    public ResponseEntity<List<ProductResponseDto>> searchProductsByName(@RequestParam String name) {
        List<ProductResponseDto> products = productService.searchByName(name);
        return ResponseEntity.ok(products);
    }
    
    // 카테고리별 상품 조회 API
    @GetMapping("/api/v1/products/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeSubCategories
    ) {
        List<ProductResponseDto> products = productService.getProductsByCategory(categoryId, includeSubCategories);
        return ResponseEntity.ok(products);
    }
    
    // 가격 범위로 상품 조회 API
    @GetMapping("/api/v1/products/price-range")
    public ResponseEntity<List<ProductResponseDto>> getProductsByPriceRange(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice
    ) {
        List<ProductResponseDto> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    // 품절 상품 조회 API
    @GetMapping("/api/v1/products/out-of-stock")
    public ResponseEntity<List<ProductResponseDto>> getOutOfStockProducts() {
        List<ProductResponseDto> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
    
    // 재고 부족 상품 조회 API (관리자 전용)
    @GetMapping("/api/v1/admin/products/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        // 관리자 정보 로깅 (필요시)
        System.out.println("Admin user: " + customUserDetails.getName() + " (" + customUserDetails.getEmail() + ") accessed low stock products");
        
        List<ProductResponseDto> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    //상품 관리 API (관리자 전용)
    // 상품 생성 API
    @PostMapping("/api/v1/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        // 상품 생성자 정보 로깅 (필요시)
        System.out.println("Admin user: " + customUserDetails.getName() + " (" + customUserDetails.getEmail() + ") created a new product: " + requestDto.name());
        
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED); // 201 Created 응답
    }

    // 특정 ID의 상품 수정 API
    @PutMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Integer id, 
            @Valid @RequestBody ProductRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        // 상품 수정자 정보 로깅 (필요시)
        System.out.println("Admin user: " + customUserDetails.getName() + " (" + customUserDetails.getEmail() + ") updated product ID: " + id);
        
        ProductResponseDto updatedProduct = productService.updateProduct(id, requestDto);
        return ResponseEntity.ok(updatedProduct); // 200 OK 응답
    }

    // 특정 ID의 상품 삭제 API
    @DeleteMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        // 상품 삭제자 정보 로깅 (필요시)
        System.out.println("Admin user: " + customUserDetails.getName() + " (" + customUserDetails.getEmail() + ") deleted product ID: " + id);
        
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }
}