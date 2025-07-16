package com.back.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;


    // 하위 카테고리 목록
    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private  List<Category> children = new ArrayList<>(); // 초기화 (nullpointexception 방지)

    // 현재 카테고리에 속한 상품 목록
    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private  List<Product> products = new ArrayList<>(); // 초기화 (nullpointexception 방지)

    // 양방향 관계 설정 시 사용하기 위한 편의 메서드
    public void addChildCategory(Category category){
        this.children.add(category);
        category.setParent(this);
    }
    public void setParent(Category parent) {
        this.parent = parent;
    }

}
