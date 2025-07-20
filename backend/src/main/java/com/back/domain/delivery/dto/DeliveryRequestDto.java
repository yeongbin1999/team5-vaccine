package com.back.domain.delivery.dto;

import com.back.domain.delivery.entity.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryRequestDto(
        @NotBlank(message = "배송 주소는 필수입니다.")
        @Size(max = 200, message = "배송 주소는 200자를 초과할 수 없습니다.")
        String address, // 배송 주소

        @Size(max = 50, message = "운송장 번호는 50자를 초과할 수 없습니다.")
        String trackingNumber, // 운송장 번호

        @Size(max = 50, message = "배송 회사는 50자를 초과할 수 없습니다.")
        String company, // 배송 회사

        // 배송 상태는 생성 시에는 기본값으로 설정되지만, 업데이트 시에는 변경 가능
        DeliveryStatus status // 배송 상태 (Enum)
) {}