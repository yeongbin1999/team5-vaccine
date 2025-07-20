package com.back.domain.product.controller;

import com.back.domain.product.dto.product.ProductResponseDto;
import com.back.domain.product.dto.product.ProductSearchDto;
import com.back.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService; // ProductService를 주입받습니다.

    //상품 조회 API (전체 사용자 접근 가능 - 인증 불필요)
    // 특정 상품을 조회하는 API
    @GetMapping("/api/v1/products/{id}")
    @Operation(summary = "특정 상품 조회",
            description = "상품 ID를 통해 특정 상품의 상세 정보를 조회합니다. 모든 사용자가 접근 가능합니다.")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Integer id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product); // 200 OK 응답
    }

    // 전체 상품을 조회하는 API
    @GetMapping("/api/v1/products")
    @Operation(summary = "전체 상품 목록 조회",
            description = "등록된 모든 상품의 목록을 조회합니다. 모든 사용자가 접근 가능합니다.")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products); // 200 OK 응답
    }

    //검색 및 필터링 API들
    // 통합 상품 검색 API
    @PostMapping("/api/v1/products/search")
    @Operation(summary = "통합 상품 검색 (POST)",
            description = "상품명, 카테고리, 가격 범위, 재고 등 다양한 조건으로 상품을 검색합니다. 요청 본문으로 검색 조건을 전달합니다.")
    public ResponseEntity<List<ProductResponseDto>> searchProducts(@Valid @RequestBody ProductSearchDto searchDto) {
        List<ProductResponseDto> products = productService.searchProducts(searchDto);
        return ResponseEntity.ok(products);
    }
    
    // GET 방식 통합 검색 API (쿼리 파라미터 사용)
    @GetMapping("/api/v1/products/search")
    @Operation(summary = "통합 상품 검색 (GET)",
            description = "쿼리 파라미터를 사용하여 상품명, 카테고리, 가격 범위, 재고 등으로 상품을 검색합니다.")
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
    @Operation(summary = "상품명으로 검색",
            description = "상품명에 검색어가 포함된 상품들을 조회합니다.")
    public ResponseEntity<List<ProductResponseDto>> searchProductsByName(@RequestParam String name) {
        List<ProductResponseDto> products = productService.searchByName(name);
        return ResponseEntity.ok(products);
    }
    
    // 카테고리별 상품 조회 API
    @GetMapping("/api/v1/products/category/{categoryId}")
    @Operation(summary = "카테고리별 상품 조회",
            description = "특정 카테고리에 속한 상품들을 조회합니다. 하위 카테고리 포함 여부를 선택할 수 있습니다.")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeSubCategories
    ) {
        List<ProductResponseDto> products = productService.getProductsByCategory(categoryId, includeSubCategories);
        return ResponseEntity.ok(products);
    }
    
    // 가격 범위로 상품 조회 API
    @GetMapping("/api/v1/products/price-range")
    @Operation(summary = "가격 범위별 상품 조회",
            description = "지정된 최소 가격과 최대 가격 사이의 상품들을 조회합니다.")
    public ResponseEntity<List<ProductResponseDto>> getProductsByPriceRange(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice
    ) {
        List<ProductResponseDto> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    // 품절 상품 조회 API
    @GetMapping("/api/v1/products/out-of-stock")
    @Operation(summary = "품절 상품 조회",
            description = "재고가 0인 품절된 상품들의 목록을 조회합니다.")
    public ResponseEntity<List<ProductResponseDto>> getOutOfStockProducts() {
        List<ProductResponseDto> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }

}
