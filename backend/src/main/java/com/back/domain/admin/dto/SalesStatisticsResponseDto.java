package com.back.domain.admin.dto;

import java.time.LocalDate;

public record SalesStatisticsResponseDto(
        LocalDate date,
        Long totalSalesAmount
) {}
