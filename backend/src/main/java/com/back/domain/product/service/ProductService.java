package com.back.domain.product.service;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.back.domain.product.dto.product.ProductResponseDto;
import com.back.domain.product.dto.product.ProductSearchDto;
import com.back.domain.product.entity.Category;
import com.back.domain.product.entity.Product;
import com.back.domain.product.exception.CategoryNotFoundException;
import com.back.domain.product.exception.ProductNotFoundException; // 아직 사용되지 않지만 나중에 필요해요.
import com.back.domain.product.repository.CategoryRepository;
import com.back.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 필요한 import 구문들을 미리 넣어둡니다. (나중에 사용할 것들도 포함)
import java.util.List; // 아직 사용되지 않지만 나중에 필요해요.
import java.util.stream.Collectors; // 아직 사용되지 않지만 나중에 필요해요.

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 상품 생성 메서드
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Category category = categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + requestDto.categoryId()));

        Product product = Product.builder()
                .name(requestDto.name())
                .imageUrl(requestDto.imageUrl())
                .price(requestDto.price())
                .stock(requestDto.stock())
                .description(requestDto.description())
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductResponseDto.from(savedProduct);
    }

    // 특정 상품(1개) 조회 메서드
    public ProductResponseDto getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        return ProductResponseDto.from(product);
    }

    // 전체 상품 조회 메서드
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.
                findAll().
                stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }

    // ========== 새로 추가된 검색 및 필터링 메서드들 ==========
    
    // 통합 검색 메서드 (모든 검색 조건을 하나의 메서드로 처리)
    public List<ProductResponseDto> searchProducts(ProductSearchDto searchDto) {
        List<Product> products;
        
        // 검색 조건이 없으면 전체 상품 반환
        if (!searchDto.hasSearchConditions()) {
            products = productRepository.findAll();
        } else {
            products = performSearch(searchDto);
        }
        
        // 재고 필터링 (품절 상품 제외가 요청된 경우)
        if (!searchDto.includeOutOfStock()) {
            products = products.stream()
                    .filter(product -> product.getStock() > 0)
                    .collect(Collectors.toList());
        }
        
        // 최소 재고 필터링
        if (searchDto.minStock() != null) {
            products = products.stream()
                    .filter(product -> product.getStock() >= searchDto.minStock())
                    .collect(Collectors.toList());
        }
        
        return products.stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // 실제 검색 로직 수행
    private List<Product> performSearch(ProductSearchDto searchDto) {
        // 가장 구체적인 조건부터 확인하여 최적화된 쿼리 실행
        
        // 1. 상품명 + 카테고리 + 가격 범위
        if (searchDto.hasNameCondition() && searchDto.hasCategoryCondition() && searchDto.hasPriceRangeCondition()) {
            Integer categoryId = getCategoryIdForSearch(searchDto);
            return productRepository.findByNameContainingIgnoreCaseAndCategoryIdAndPriceBetween(
                    searchDto.name().trim(), categoryId, 
                    getMinPrice(searchDto), getMaxPrice(searchDto)
            );
        }
        
        // 2. 상품명 + 카테고리
        if (searchDto.hasNameCondition() && searchDto.hasCategoryCondition()) {
            Integer categoryId = getCategoryIdForSearch(searchDto);
            return productRepository.findByNameContainingIgnoreCaseAndCategoryId(
                    searchDto.name().trim(), categoryId
            );
        }
        
        // 3. 상품명 + 가격 범위
        if (searchDto.hasNameCondition() && searchDto.hasPriceRangeCondition()) {
            return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                    searchDto.name().trim(), getMinPrice(searchDto), getMaxPrice(searchDto)
            );
        }
        
        // 4. 카테고리 + 가격 범위
        if (searchDto.hasCategoryCondition() && searchDto.hasPriceRangeCondition()) {
            Integer categoryId = getCategoryIdForSearch(searchDto);
            return productRepository.findByCategoryIdAndPriceBetween(
                    categoryId, getMinPrice(searchDto), getMaxPrice(searchDto)
            );
        }
        
        // 5. 상품명만
        if (searchDto.hasNameCondition()) {
            return productRepository.findByNameContainingIgnoreCase(searchDto.name().trim());
        }
        
        // 6. 카테고리만
        if (searchDto.hasCategoryCondition()) {
            Integer categoryId = getCategoryIdForSearch(searchDto);
            if (searchDto.includeSubCategories()) {
                return productRepository.findByCategoryIdIncludingChildren(categoryId);
            } else {
                return productRepository.findByCategoryId(categoryId);
            }
        }
        
        // 7. 가격 범위만
        if (searchDto.hasPriceRangeCondition()) {
            return productRepository.findByPriceBetween(getMinPrice(searchDto), getMaxPrice(searchDto));
        }
        
        // 기본값: 전체 상품
        return productRepository.findAll();
    }
    
    // 카테고리 ID 결정 (하위 카테고리 포함 여부 고려)
    private Integer getCategoryIdForSearch(ProductSearchDto searchDto) {
        return searchDto.categoryId();
    }
    
    // 최소 가격 결정
    private Integer getMinPrice(ProductSearchDto searchDto) {
        return searchDto.minPrice() != null ? searchDto.minPrice() : 0;
    }
    
    // 최대 가격 결정
    private Integer getMaxPrice(ProductSearchDto searchDto) {
        return searchDto.maxPrice() != null ? searchDto.maxPrice() : Integer.MAX_VALUE;
    }
    
    // 상품명으로 검색
    public List<ProductResponseDto> searchByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return products.stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // 카테고리별 상품 조회
    public List<ProductResponseDto> getProductsByCategory(Integer categoryId, boolean includeSubCategories) {
        List<Product> products;
        
        if (includeSubCategories) {
            products = productRepository.findByCategoryIdIncludingChildren(categoryId);
        } else {
            products = productRepository.findByCategoryId(categoryId);
        }
        
        return products.stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // 가격 범위로 상품 조회
    public List<ProductResponseDto> getProductsByPriceRange(Integer minPrice, Integer maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // 품절 상품 조회
    public List<ProductResponseDto> getOutOfStockProducts() {
        List<Product> products = productRepository.findByStock(0);
        return products.stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // 재고가 부족한 상품 조회 (임계값 이하)
    public List<ProductResponseDto> getLowStockProducts(Integer threshold) {
        List<Product> allProducts = productRepository.findAll();
        List<Product> lowStockProducts = allProducts.stream()
                .filter(product -> product.getStock() <= threshold && product.getStock() > 0)
                .collect(Collectors.toList());
        
        return lowStockProducts.stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }

    // ========== 기존 메서드들 ==========

    // 특정 상품(1개) 업데이트 메서드
    @Transactional
    public ProductResponseDto updateProduct(Integer id, ProductRequestDto requestDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        Category newCategory = null;
        if (requestDto.categoryId() != null) {
            newCategory = categoryRepository.findById(requestDto.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + requestDto.categoryId()));
        }

        product.setName(requestDto.name());
        product.setImageUrl(requestDto.imageUrl());
        product.setPrice(requestDto.price());
        product.setStock(requestDto.stock());
        product.setDescription(requestDto.description());
        product.setCategory(newCategory);
        //productRepository.save(product); 명시적으로 호출 안해도 됌

        return ProductResponseDto.from(product);
    }

    // 특정 상품(1개) 삭제 메서드
    @Transactional
    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }
}