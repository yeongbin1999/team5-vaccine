package com.back.domain.cart.repository;

import com.back.domain.cart.entity.Cart;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    @Query("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product p WHERE c.user = :user")
    Optional<Cart> findByUser(@Param("user") User user);
}
