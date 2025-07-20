package com.back.domain.admin.dto;

import com.back.domain.order.entity.OrderStatus;

import java.time.LocalDate;

public record OrderStatisticsResponseDto(
        LocalDate date,
        OrderStatus status,
        Long orderCount,
        Long totalRevenue
) {
    public static OrderStatisticsResponseDto of(LocalDate date, OrderStatus status, Long orderCount, Long totalRevenue) {
        return new OrderStatisticsResponseDto(date, status, orderCount, totalRevenue);
    }
}
