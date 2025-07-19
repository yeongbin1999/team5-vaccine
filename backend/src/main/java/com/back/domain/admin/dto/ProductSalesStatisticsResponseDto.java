package com.back.domain.admin.dto;

public record ProductSalesStatisticsResponseDto(
        Integer productId,
        String productName,
        Long totalQuantitySold,
        Long totalSalesAmount
) {}
