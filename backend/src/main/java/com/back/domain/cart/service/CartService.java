package com.back.domain.cart.service;

import com.back.domain.cart.dto.AddCartItemRequest;
import com.back.domain.cart.dto.CartDto;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // CartService는 CartItemRepository를 사용하여 장바구니 아이템을 관리합니다.
    // CartService는 CartItemRepository를 통해 장바구니 아이템을 추가, 수정, 삭제하는 기능을 제공합니다.
    public void addItem(int userId, AddCartItemRequest request) {
        // 장바구니 아이템 추가 로직
        // 1. 사용자와 상품을 조회합니다.
        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(request.productId()).orElseThrow();
        // 2. 해당 사용자의 장바구니를 조회합니다.
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
        // 3. 장바구니에 해당 상품이 이미 존재하는지 확인합니다.
        // 4. 존재하면 수량을 업데이트하고, 존재하지 않으면 새로 추가합니다.
        Optional<CartItem> optionalItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (optionalItem.isPresent()) {
            CartItem item = optionalItem.get();
            item.updateQuantity(item.getQuantity() + request.quantity());
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            cartItemRepository.save(item);
        }
    }
    // CartService는 CartItemRepository를 통해 장바구니 아이템을 조회하는 기능을 제공합니다.
    public CartDto getCart(Integer userId) {
        User user = userRepository.findById(userId)
                 .orElseThrow();
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("장바구니가 존재하지 않습니다."));
        return CartDto.from(cart);
    }
    // CartService는 CartItemRepository를 통해 장바구니 아이템의 수량을 업데이트하는 기능을 제공합니다.
    public void updateItemQuantity(Integer CartItemId,int quantity) {
        CartItem cartItem = cartItemRepository.findById(CartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 아이템이 존재하지 않습니다."));
        cartItem.updateQuantity(quantity);
    }

    // CartService는 CartItemRepository를 통해 장바구니 아이템을 삭제하는 기능을 제공합니다.
    public void deleteItem(Integer CartItemId) {
        cartItemRepository.deleteById(CartItemId);
    }

    // CartService는 CartRepository를 통해 장바구니를 비우는 기능을 제공합니다.
    public void clearCart(Integer userId) {
        Cart cart = cartRepository.findByUser(userRepository.findById(userId).orElseThrow())
                .orElseThrow();
        cart.clearItems();
    }
}
