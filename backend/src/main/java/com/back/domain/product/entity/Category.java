package com.back.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String name;

    // 부모 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // 하위 카테고리 목록
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Category> children = new ArrayList<>(); // 초기화 (nullpointexception 방지)

    // 현재 카테고리에 속한 상품 목록
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Product> products = new ArrayList<>(); // 초기화 (nullpointexception 방지)

    // 양방향 관계 설정 시 사용하기 위한 편의 메서드
    public void addChildCategory(Category category) {
        this.children.add(category);
        category.parent = this;
    }

    public void removeChildCategory(Category category) {
        this.children.remove(category);
        category.parent = null;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }
}
