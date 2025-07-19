package com.back.domain.delivery.dto;

import com.back.domain.delivery.entity.Delivery;
import com.back.domain.delivery.entity.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryResponseDto(
        Integer id,
        String address,
        LocalDateTime startDate,
        LocalDateTime completeDate,
        String trackingNumber,
        DeliveryStatus status,
        String company
) {
    // Delivery 엔티티를 받아서 DeliveryResponseDto를 생성하는 팩토리 메서드
    public static DeliveryResponseDto fromEntity(Delivery delivery) {
        return new DeliveryResponseDto(
                delivery.getId(),
                delivery.getAddress(),
                delivery.getStartDate(),
                delivery.getCompleteDate(),
                delivery.getTrackingNumber(),
                delivery.getStatus(),
                delivery.getCompany()
        );
    }
}
