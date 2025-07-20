package com.back.domain.order.service;

import com.back.domain.delivery.entity.Delivery;
import com.back.domain.order.dto.order.OrderDetailDTO;
import com.back.domain.order.dto.order.OrderListDTO;
import com.back.domain.order.dto.order.OrderRequestDTO;
import com.back.domain.order.dto.order.OrderStatusUpdateDTO;
import com.back.domain.order.dto.orderitem.OrderItemRequestDTO;
import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    // 1. 주문 생성
    public OrderDetailDTO createOrder(OrderRequestDTO request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Delivery deliveryRef = entityManager.getReference(Delivery.class, request.deliveryId());

        Order order = new Order();
        order.setUser(user);
        order.setAddress(request.address());
        order.setDelivery(deliveryRef);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.배송준비중);

        Order savedOrder = orderRepository.save(order);
        int totalPrice = 0;

        for (OrderItemRequestDTO item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.quantity());
            orderItem.setUnitPrice(item.unitPrice());

            orderItemRepository.save(orderItem);
            totalPrice += item.unitPrice() * item.quantity();
        }

        savedOrder.setTotalPrice(totalPrice);
        return OrderDetailDTO.from(savedOrder, orderItemRepository.findByOrder(savedOrder));
    }

    // 2. 내 주문 목록 조회
    public List<OrderListDTO> getMyOrders(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return orderRepository.findByUser(user)
                .stream()
                .map(OrderListDTO::from)
                .toList();
    }

    // 3. 주문 상세 조회 (보안 적용)
    public OrderDetailDTO getOrderDetail(int orderId, int userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));

        if (order.getUser().getId() != userId) {
            throw new SecurityException("본인의 주문만 조회할 수 있습니다.");
        }

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return OrderDetailDTO.from(order, items);
    }

    // 4. 관리자 - 전체 주문 목록
    public List<OrderListDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderListDTO::from)
                .toList();
    }

    // 5. 관리자 - 주문 상태 변경
    public OrderDetailDTO updateOrderStatus(OrderStatusUpdateDTO dto) {
        Order order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));
        order.setStatus(dto.newStatus());
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return OrderDetailDTO.from(order, items);
    }
}
