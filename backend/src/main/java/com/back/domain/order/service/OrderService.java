package com.back.domain.order.service;

import com.back.domain.admin.dto.OrderStatisticsResponseDto;
import com.back.domain.delivery.entity.Delivery;
import com.back.domain.order.dto.order.OrderDetailDTO;
import com.back.domain.order.dto.order.OrderListDTO;
import com.back.domain.order.dto.order.OrderRequestDTO;
import com.back.domain.order.dto.order.OrderStatusUpdateDTO;
import com.back.domain.order.dto.orderitem.OrderItemRequestDTO;
import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.exception.InsufficientStockException;
import com.back.domain.order.exception.OrderNotFoundException;
import com.back.domain.order.exception.UnauthorizedOrderAccessException;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.exception.ProductNotFoundException;
import com.back.domain.product.repository.ProductRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    @Transactional
    public OrderDetailDTO createOrder(OrderRequestDTO request) {
        // 사용자 검증
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + request.userId()));
        
        // 배송 정보 존재 여부 검증
        Delivery delivery = entityManager.find(Delivery.class, request.deliveryId());
        if (delivery == null) {
            throw new IllegalArgumentException("배송 정보를 찾을 수 없습니다. ID: " + request.deliveryId());
        }

        // 주문 생성 (totalPrice는 나중에 계산)
        Order order = Order.builder()
                .user(user)
                .address(request.address())
                .delivery(delivery)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.배송준비중)
                .totalPrice(0)
                .build();

        // 주문 항목 처리 및 재고 검증 (주문에 직접 추가)
        List<OrderItem> orderItems = processOrderItems(request.items(), order);
        
        // 총 금액 계산 및 설정
        order.calculateTotalPrice();

        // 한 번에 저장 (Cascade로 OrderItem도 함께 저장됨)
        Order savedOrder = orderRepository.save(order);

        return OrderDetailDTO.from(savedOrder, orderItems);
    }

    private List<OrderItem> processOrderItems(List<OrderItemRequestDTO> itemRequests, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemRequestDTO itemRequest : itemRequests) {
            // 상품 조회
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다. ID: " + itemRequest.productId()));

            // 재고 검증
            if (product.getStock() < itemRequest.quantity()) {
                throw new InsufficientStockException(
                        product.getName(), itemRequest.quantity(), product.getStock());
            }
            
            // 재고 차감
            product.setStock(product.getStock() - itemRequest.quantity());

            // 주문 항목 생성 및 주문에 추가
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .build();

            // 주문에 직접 추가 (양방향 관계 설정)
            order.addOrderItem(orderItem);
            orderItems.add(orderItem);
        }
        
        return orderItems;
    }

    public List<OrderListDTO> getMyOrders(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        return orderRepository.findByUserOrderByOrderDateDesc(user)
                .stream()
                .map(OrderListDTO::from)
                .toList();
    }

    public OrderDetailDTO getOrderDetail(Integer orderId, Integer userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> {
                    // 주문이 존재하는지 먼저 확인
                    if (orderRepository.existsById(orderId)) {
                        // 주문은 존재하지만 권한이 없는 경우
                        throw new UnauthorizedOrderAccessException();
                    } else {
                        // 주문 자체가 존재하지 않는 경우
                        throw new OrderNotFoundException(orderId);
                    }
                });

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return OrderDetailDTO.from(order, items);
    }

    // 관리자 전용 메서드들
    public List<OrderListDTO> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(OrderListDTO::from)
                .toList();
    }

    // 3. 주문 상세 조회 (관리자는 모두 가능, 일반 사용자는 본인 주문만)
    public OrderDetailDTO getOrderDetail(int orderId, int userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 관리자라면 모든 주문 조회 가능
        if (!isAdmin(user) && order.getUser().getId() != userId) {
            throw new SecurityException("본인의 주문만 조회할 수 있습니다.");
        }
        return orders.map(OrderListDTO::from);
    }

    public OrderDetailDTO getOrderDetailForAdmin(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return OrderDetailDTO.from(order, items);
    }

    private boolean isAdmin(User user) {
        // 예: User 엔티티에 getRole()이 있다고 가정
        return user.getRole() == Role.ADMIN;
    }


    // 4. 관리자 - 전체 주문 목록
    public List<OrderListDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .flatMap(statusMap -> statusMap.values().stream())
                .toList();
    }

    @Transactional
    public OrderDetailDTO updateOrderStatus(OrderStatusUpdateDTO dto) {
        Order order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new OrderNotFoundException(dto.orderId()));
        
        order.setStatus(dto.newStatus());
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return OrderDetailDTO.from(order, items);
    }
}
