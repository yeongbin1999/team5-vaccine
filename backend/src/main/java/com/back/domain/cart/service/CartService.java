package com.back.domain.cart.service;

import com.back.domain.cart.dto.AddCartItemRequest;
import com.back.domain.cart.dto.CartDto;
import com.back.domain.cart.dto.UpdateCartItemRequest;
import com.back.domain.cart.entity.Cart;
import com.back.domain.cart.entity.CartItem;
import com.back.domain.cart.repository.CartItemRepository;
import com.back.domain.cart.repository.CartRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException; // NoSuchElementException 임포트
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addItem(Integer userId, AddCartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. userId: " + userId));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다. productId: " + request.productId()));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        if (product.getStock() < request.quantity()) {
            throw new IllegalArgumentException("상품의 재고가 부족합니다. 현재 재고: " + product.getStock());
        }

        Optional<CartItem> optionalItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (optionalItem.isPresent()) {
            CartItem item = optionalItem.get();
            int newTotalQuantity = item.getQuantity() + request.quantity();
            if (product.getStock() < newTotalQuantity) {
                throw new IllegalArgumentException("상품의 재고가 부족하여 더 이상 추가할 수 없습니다. 현재 재고: " + product.getStock());
            }
            item.updateQuantity(newTotalQuantity);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            cartItemRepository.save(item);
            cart.addCartItem(item);
        }
    }

    @Transactional(readOnly = true)
    public CartDto getCart(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. userId: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        return CartDto.from(cart);
    }

    @Transactional
    public void updateItemQuantity(Integer cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("장바구니 항목을 찾을 수 없습니다. cartItemId: " + cartItemId));

        Product product = cartItem.getProduct();
        Integer newQuantity = request.quantity();

        if (newQuantity == 0) {
            deleteItem(cartItemId);
            return;
        }

        if (product.getStock() < newQuantity) {
            throw new IllegalArgumentException("요청한 수량이 상품 재고를 초과합니다. 현재 재고: " + product.getStock());
        }

        cartItem.updateQuantity(newQuantity);
    }

    @Transactional
    public void deleteItem(Integer cartItemId) {
        CartItem itemToDelete = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("장바구니 항목을 찾을 수 없습니다. ID: " + cartItemId));

        Cart cart = itemToDelete.getCart();
        cart.removeCartItem(itemToDelete);
        cartItemRepository.delete(itemToDelete);
    }

    @Transactional
    public void clearCart(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. userId: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자의 장바구니를 찾을 수 없습니다. userId: " + userId));

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.clearCartItems();
    }
}