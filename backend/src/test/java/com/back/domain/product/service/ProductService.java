package com.back.domain.product.service;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.back.domain.product.dto.product.ProductResponseDto;
import com.back.domain.product.entity.Category;
import com.back.domain.product.entity.Product;
import com.back.domain.product.exception.CategoryNotFoundException;
import com.back.domain.product.exception.ProductNotFoundException;
import com.back.domain.product.repository.CategoryRepository;
import com.back.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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