package com.back.domain.order.entity;

import com.back.domain.delivery.entity.Delivery;
import com.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "order_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime orderDate;

    @Column(name = "shipping_address", length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('배송준비중','배송중','배송완료','취소') DEFAULT '배송준비중'")
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", foreignKey = @ForeignKey(name = "fk_order_delivery"))
    private Delivery delivery;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = OrderStatus.배송준비중;
        }
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (totalPrice == null) {
            totalPrice = 0;
        }
    }

    // 비즈니스 메서드
    public void addOrderItem(OrderItem orderItem) {
        items.add(orderItem);
        orderItem.setOrder(this);
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    public boolean isOwner(Integer userId) {
        return this.user.getId().equals(userId);
    }

    public void removeOrderItem(OrderItem orderItem) {
        items.remove(orderItem);
        orderItem.setOrder(null);
        // 총 가격 재계산
        calculateTotalPrice();
    }
}
