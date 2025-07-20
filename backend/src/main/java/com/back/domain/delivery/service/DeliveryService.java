package com.back.domain.delivery.service;

import com.back.domain.delivery.dto.DeliveryRequestDto;
import com.back.domain.delivery.dto.DeliveryResponseDto;
import com.back.domain.delivery.entity.Delivery;
import com.back.domain.delivery.entity.DeliveryStatus;
import com.back.domain.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 설정
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    /**
     * 새로운 배송 정보를 생성합니다.
     *
     * @param request 생성할 배송 정보 DTO
     * @return 생성된 배송 정보 DTO
     */
    @Transactional
    public DeliveryResponseDto createDelivery(DeliveryRequestDto request) {
        // DeliveryStatus는 DTO에서 받아오거나, 엔티티의 @Builder.Default에 의해 '배송준비중'으로 자동 설정됩니다.
        // trackingNumber와 company는 선택 사항이므로 null일 수 있습니다.
        Delivery delivery = Delivery.builder()
                .address(request.address())
                .trackingNumber(request.trackingNumber())
                .company(request.company())
                // startDate는 @CreatedDate에 의해 자동 설정되므로 수동 설정 제거
                .status(request.status() != null ? request.status() : DeliveryStatus.배송준비중) // DTO에 상태가 있으면 사용, 없으면 기본값
                .build();
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return DeliveryResponseDto.fromEntity(savedDelivery);
    }

    /**
     * 모든 배송 정보를 페이지네이션하여 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 배송 정보 목록과 페이지 정보를 담은 PageResponseDto
     */
    public Page<DeliveryResponseDto> getAllDeliveries(Pageable pageable) {
        return deliveryRepository.findAll(pageable)
                .map(DeliveryResponseDto::fromEntity);
    }

    /**
     * 특정 ID의 배송 정보를 조회합니다.
     *
     * @param deliveryId 조회할 배송 ID
     * @return 조회된 배송 정보 DTO
     * @throws NoSuchElementException 배송 정보를 찾을 수 없을 경우
     */
    public DeliveryResponseDto getDeliveryById(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoSuchElementException("배송 정보를 찾을 수 없습니다. ID: " + deliveryId));
        return DeliveryResponseDto.fromEntity(delivery);
    }

    /**
     * 특정 ID의 배송 정보를 업데이트합니다.
     *
     * @param deliveryId 업데이트할 배송 ID
     * @param request 업데이트할 배송 정보 DTO
     * @return 업데이트된 배송 정보 DTO
     * @throws NoSuchElementException 배송 정보를 찾을 수 없을 경우
     */
    @Transactional
    public DeliveryResponseDto updateDelivery(Integer deliveryId, DeliveryRequestDto request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoSuchElementException("배송 정보를 찾을 수 없습니다. ID: " + deliveryId));

        // 엔티티의 updateDetails 메서드를 사용하여 필드 업데이트
        delivery.updateDetails(
                request.address(),
                request.trackingNumber(),
                request.company(),
                request.status()
        );
        // @Transactional에 의해 변경 감지(Dirty Checking)로 자동 저장됩니다.
        return DeliveryResponseDto.fromEntity(delivery);
    }

    /**
     * 특정 ID의 배송 상태를 업데이트합니다.
     * (이 메서드는 주문 도메인에서 배송 상태를 변경할 때 유용할 수 있습니다.)
     *
     * @param deliveryId 업데이트할 배송 ID
     * @param status 변경할 배송 상태
     * @return 업데이트된 배송 정보 DTO
     * @throws NoSuchElementException 배송 정보를 찾을 수 없을 경우
     */
    @Transactional
    public DeliveryResponseDto updateDeliveryStatus(Integer deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoSuchElementException("배송 정보를 찾을 수 없습니다. ID: " + deliveryId));
        delivery.updateStatus(status);
        return DeliveryResponseDto.fromEntity(delivery);
    }

    /**
     * 특정 ID의 배송 정보를 삭제합니다.
     *
     * @param deliveryId 삭제할 배송 ID
     * @throws NoSuchElementException 배송 정보를 찾을 수 없을 경우
     */
    @Transactional
    public void deleteDelivery(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoSuchElementException("배송 정보를 찾을 수 없습니다. ID: " + deliveryId));
        deliveryRepository.delete(delivery);
    }
}