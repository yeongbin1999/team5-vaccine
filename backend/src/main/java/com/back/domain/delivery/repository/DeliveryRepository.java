package com.back.domain.delivery.repository;

import com.back.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    // 운송장 번호로 배송 정보를 조회하는 메서드 (선택 사항)
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
}
