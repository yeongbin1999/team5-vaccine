package com.back.domain.cart.repository;

import com.back.domain.cart.entity.Cart;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}