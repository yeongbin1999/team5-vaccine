package com.back.global.config;

import com.back.domain.cart.entity.Cart;
import com.back.domain.cart.entity.CartItem;
import com.back.domain.cart.repository.CartItemRepository;
import com.back.domain.cart.repository.CartRepository;
import com.back.domain.delivery.entity.Delivery;
import com.back.domain.delivery.entity.DeliveryStatus;
import com.back.domain.delivery.repository.DeliveryRepository;
import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Category;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.CategoryRepository;
import com.back.domain.product.repository.ProductRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               CartRepository cartRepository,
                               CartItemRepository cartItemRepository,
                               OrderRepository orderRepository,
                               OrderItemRepository orderItemRepository,
                               DeliveryRepository deliveryRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {

            Random random = new Random();

            // 1. 사용자 20명 (1명 관리자)
            List<User> users = new ArrayList<>();
            users.add(User.builder()
                    .name("관리자")
                    .email("admin@test.com")
                    .password(passwordEncoder.encode("admin"))
                    .address("서울시 송파구")
                    .phone("010-3333-4444")
                    .role(Role.ADMIN)
                    .build());

            for (int i = 1; i <= 19; i++) {
                users.add(User.builder()
                        .name("유저" + i)
                        .email("user" + i + "@test.com")
                        .password(passwordEncoder.encode("pass" + i))
                        .address("서울시 랜덤구" + i)
                        .phone("010-" + (1000 + i) + "-" + (2000 + i))
                        .role(Role.USER)
                        .build());
            }
            userRepository.saveAll(users);

            // 2. 카테고리 20개 (10개 부모, 10개 자식)
            List<Category> categories = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                Category parent = (i > 10) ? categories.get(random.nextInt(10)) : null;
                categories.add(Category.builder()
                        .name("카테고리" + i)
                        .parent(parent)
                        .build());
            }
            categoryRepository.saveAll(categories);

            // 3. 상품 20개
            List<Product> products = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                Category randomCategory = categories.get(random.nextInt(categories.size()));
                products.add(Product.builder()
                        .name("상품" + i)
                        .imageUrl("https://dummyimg.com/product" + i + ".jpg")
                        .price(10000 + (i * 500))
                        .stock(10 + random.nextInt(50))
                        .description("상품" + i + "에 대한 설명입니다.")
                        .category(randomCategory)
                        .build());
            }
            productRepository.saveAll(products);

            // 4. 장바구니 20개 (유저별 1개)
            List<Cart> carts = new ArrayList<>();
            for (User user : users) {
                carts.add(Cart.builder()
                        .user(user)
                        .build());
            }
            cartRepository.saveAll(carts);

            // 5. 장바구니 아이템 (2~3개 상품 랜덤 추가)
            List<CartItem> cartItems = new ArrayList<>();
            for (Cart cart : carts) {
                int itemCount = 2 + random.nextInt(2);
                for (int j = 0; j < itemCount; j++) {
                    Product randomProduct = products.get(random.nextInt(products.size()));
                    cartItems.add(CartItem.builder()
                            .cart(cart)
                            .product(randomProduct)
                            .quantity(1 + random.nextInt(3))
                            .build());
                }
            }
            cartItemRepository.saveAll(cartItems);

            // 6. 배송 20개 생성
            String[] companies = {"CJ대한통운", "한진택배", "로젠택배", "우체국택배"};
            DeliveryStatus[] deliveryStatuses = DeliveryStatus.values();
            List<Delivery> deliveries = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                deliveries.add(Delivery.builder()
                        .address("서울시 랜덤구 " + i)
                        .trackingNumber("TRK" + (100000 + i))
                        .company(companies[random.nextInt(companies.length)])
                        .status(deliveryStatuses[random.nextInt(deliveryStatuses.length)])
                        .build());
            }
            deliveryRepository.saveAll(deliveries);

            // 7. 주문 20개 생성 (랜덤 유저, 랜덤 배송)
            OrderStatus[] orderStatuses = OrderStatus.values();
            List<Order> orders = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                User randomUser = users.get(random.nextInt(users.size()));
                Delivery randomDelivery = deliveries.get(random.nextInt(deliveries.size()));

                int totalPrice = 20000 + (i * 1000);
                OrderStatus status = orderStatuses[random.nextInt(orderStatuses.length)];

                orders.add(Order.builder()
                        .user(randomUser)
                        .address(randomUser.getAddress())
                        .totalPrice(totalPrice)
                        .status(status)
                        .delivery(randomDelivery)
                        .build());
            }
            orderRepository.saveAll(orders);

            // 8. 주문상품 20개 생성 (랜덤 주문당 1~3개 상품)
            List<OrderItem> orderItems = new ArrayList<>();
            for (Order order : orders) {
                int itemCount = 1 + random.nextInt(3);
                for (int j = 0; j < itemCount; j++) {
                    Product randomProduct = products.get(random.nextInt(products.size()));
                    orderItems.add(OrderItem.builder()
                            .order(order)
                            .product(randomProduct)
                            .quantity(1 + random.nextInt(3))
                            .unitPrice(randomProduct.getPrice())
                            .build());
                }
            }
            orderItemRepository.saveAll(orderItems);

            System.out.println("dev 모드 초기 데이터 20개씩 생성");
        };
    }
}
