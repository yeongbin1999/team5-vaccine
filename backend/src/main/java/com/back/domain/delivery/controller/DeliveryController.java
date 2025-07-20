package com.back.domain.delivery.controller;

import com.back.domain.admin.dto.PageResponseDto;
import com.back.domain.delivery.dto.DeliveryRequestDto;
import com.back.domain.delivery.dto.DeliveryResponseDto;
import com.back.domain.delivery.entity.DeliveryStatus;
import com.back.domain.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/deliveries") // 관리자 전용 API
@RequiredArgsConstructor
@Validated // @Positive와 같은 파라미터 레벨 유효성 검사를 위해 추가
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 새로운 배송 정보를 생성합니다.
     *
     * @param request 생성할 배송 정보 DTO
     * @return 생성된 배송 정보 DTO
     */
    @PostMapping
    @Operation(summary = "관리자 - 새로운 배송 정보 생성",
            description = "새로운 배송 정보를 생성합니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponseDto> createDelivery(@RequestBody @Valid DeliveryRequestDto request) {
        DeliveryResponseDto response = deliveryService.createDelivery(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 모든 배송 정보를 페이지네이션하여 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 배송 정보 목록과 페이지 정보를 담은 응답
     */
    @GetMapping
    @Operation(summary = "관리자 - 모든 배송 정보 조회 (페이지네이션 가능)",
            description = "모든 배송 정보 목록을 조회합니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseDto<DeliveryResponseDto>> getAllDeliveries(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<DeliveryResponseDto> deliveriesPage = deliveryService.getAllDeliveries(pageable);
        PageResponseDto<DeliveryResponseDto> responseDto = new PageResponseDto<>(
                deliveriesPage.getContent(),
                deliveriesPage.getNumber(), // pageNo -> pageNumber
                deliveriesPage.getSize(),
                deliveriesPage.getTotalElements(),
                deliveriesPage.getTotalPages(),
                deliveriesPage.isLast() // last -> isLast
        );
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 특정 ID의 배송 정보를 조회합니다.
     *
     * @param deliveryId 조회할 배송 ID
     * @return 조회된 배송 정보 DTO
     */
    @GetMapping("/{deliveryId}")
    @Operation(summary = "관리자 - 특정 배송 정보 조회",
            description = "특정 배송 ID를 통해 상세 정보를 조회합니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponseDto> getDeliveryById(
            @PathVariable @Positive(message = "배송 ID는 양수여야 합니다.") Integer deliveryId) {
        DeliveryResponseDto response = deliveryService.getDeliveryById(deliveryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 ID의 배송 정보를 업데이트합니다.
     *
     * @param deliveryId 업데이트할 배송 ID
     * @param request    업데이트할 배송 정보 DTO
     * @return 업데이트된 배송 정보 DTO
     */
    @PutMapping("/{deliveryId}")
    @Operation(summary = "관리자 - 특정 배송 정보 업데이트",
            description = "특정 배송 ID의 정보를 업데이트합니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponseDto> updateDelivery(
            @PathVariable @Positive(message = "배송 ID는 양수여야 합니다.") Integer deliveryId,
            @RequestBody @Valid DeliveryRequestDto request) {
        DeliveryResponseDto response = deliveryService.updateDelivery(deliveryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 ID의 배송 상태를 업데이트합니다.
     * (주문 도메인에서 배송 상태를 변경할 때 사용될 수 있습니다.)
     *
     * @param deliveryId 업데이트할 배송 ID
     * @param status     변경할 배송 상태 (쿼리 파라미터)
     * @return 업데이트된 배송 정보 DTO
     */
    @PatchMapping("/{deliveryId}/status")
    @Operation(summary = "관리자 - 특정 배송 상태 업데이트",
            description = "특정 배송 ID의 상태를 업데이트합니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponseDto> updateDeliveryStatus(
            @PathVariable @Positive(message = "배송 ID는 양수여야 합니다.") Integer deliveryId,
            @RequestParam @NotNull(message = "배송 상태는 필수입니다.") DeliveryStatus status) {
        DeliveryResponseDto response = deliveryService.updateDeliveryStatus(deliveryId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 ID의 배송 정보를 삭제합니다.
     *
     * @param deliveryId 삭제할 배송 ID
     * @return 응답 없음 (HTTP 204 No Content)
     */
    @DeleteMapping("/{deliveryId}")
    @Operation(summary = "관리자 - 특정 배송 정보 삭제",
            description = "특정 배송 ID의 정보를 삭제합니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDelivery(
            @PathVariable @Positive(message = "배송 ID는 양수여야 합니다.") Integer deliveryId) {
        deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}