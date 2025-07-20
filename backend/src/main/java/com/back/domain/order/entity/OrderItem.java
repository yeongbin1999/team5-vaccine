package com.back.domain.order.entity;

import com.back.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    public Integer getTotalPrice() {
        return this.unitPrice * this.quantity;
    }

    // 편의 메서드
    public void changeQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void changeUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }
}
