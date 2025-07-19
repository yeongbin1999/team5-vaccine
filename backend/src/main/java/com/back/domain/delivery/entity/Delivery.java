package com.back.domain.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Delivery {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id; // int -> Integer로 변경

    @Column(nullable = false, length = 200)
    private String address;

    @CreatedDate // 엔티티 생성 시 자동으로 현재 시간 설정
    @Column(name = "start_date", updatable = false)
    private LocalDateTime startDate;

    @Column(name = "complete_date")
    private LocalDateTime completeDate;

    @Column(length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default // 빌더 패턴 사용 시 기본값 설정
    private DeliveryStatus status = DeliveryStatus.배송준비중; // 기본값 '배송준비중' 설정

    @Column(length = 50)
    private String company;

    /**
     * 배송 상태를 업데이트합니다.
     * 상태가 '배송완료'로 변경될 경우 completeDate를 현재 시각으로 설정합니다.
     *
     * @param status 새로운 배송 상태
     */
    public void updateStatus(DeliveryStatus status) {
        this.status = status;
        if (status == DeliveryStatus.배송완료 && this.completeDate == null) {
            this.completeDate = LocalDateTime.now();
        }
    }

    /**
     * 배송 상세 정보를 업데이트합니다. (주소, 운송장 번호, 회사, 상태)
     * 각 필드는 null이 아닐 경우에만 업데이트됩니다.
     *
     * @param address 새로운 배송 주소 (null 허용)
     * @param trackingNumber 새로운 운송장 번호 (null 허용)
     * @param company 새로운 배송 회사 (null 허용)
     * @param status 새로운 배송 상태 (null 허용)
     */
    public void updateDetails(String address, String trackingNumber, String company, DeliveryStatus status) {
        if (address != null) {
            this.address = address;
        }
        if (trackingNumber != null) {
            this.trackingNumber = trackingNumber;
        }
        if (company != null) {
            this.company = company;
        }
        if (status != null) {
            updateStatus(status); // 상태 업데이트는 별도 메서드 재활용
        }
    }

    /**
     * 배송 시작일을 수동으로 설정합니다. (필요한 경우 사용)
     * @param startDate 배송 시작일
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
}