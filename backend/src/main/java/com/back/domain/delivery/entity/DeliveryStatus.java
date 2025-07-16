package com.back.domain.delivery.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {
    배송준비중("READY", "상품을 배송하기 위해 준비 중입니다."),
    배송중("IN_DELIVERY", "상품이 현재 배송 중입니다."),
    배송완료("COMPLETED", "상품 배송이 완료되었습니다.");

    private final String code;
    private final String description;

    // code를 통해 Enum을 찾아오는 메서드 (선택 사항)
    public static DeliveryStatus fromCode(String code) {
        for (DeliveryStatus status : DeliveryStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown DeliveryStatus code: " + code);
    }
}