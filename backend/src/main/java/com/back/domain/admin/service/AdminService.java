package com.back.domain.admin.service;

import com.back.domain.admin.dto.PageResponseDto;
import com.back.domain.admin.dto.ProductSalesStatisticsResponseDto;
import com.back.domain.admin.dto.SalesStatisticsResponseDto;
import com.back.domain.user.dto.UpdateUserRequest;
import com.back.domain.user.dto.UserResponse;

import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 관리자 - 모든 사용자 목록을 페이지네이션 및 검색 기능과 함께 조회합니다.
     * (여기서의 사용자는 서비스 사용자(고객)를 의미합니다.)
     *
     * @param pageable 페이지 정보 (페이지 번호, 페이지 크기, 정렬)
     * @param search 검색어 (이메일 또는 이름으로 검색)
     * @return 사용자 목록과 페이지 정보를 담은 PageResponseDto
     */
    public PageResponseDto<UserResponse> getAllUsers(Pageable pageable, String search) {
        Page<User> userPage;
        if (search != null && !search.trim().isEmpty()) {
            // 변경된 메서드 이름 findByEmail 사용 (이메일 또는 이름 검색)
            userPage = userRepository.findByEmail(search, search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        List<UserResponse> userDtos = userPage.getContent().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                userDtos,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }

    /**
     * 관리자 - 특정 사용자 ID로 사용자 상세 정보를 조회합니다.
     * (여기서의 사용자는 서비스 사용자(고객)를 의미합니다.)
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 상세 정보 UserResponse
     * @throws NoSuchElementException 해당 ID의 사용자를 찾을 수 없을 경우
     */
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));
        return UserResponse.from(user);
    }

    /**
     * 관리자 - 특정 사용자 ID의 정보를 수정합니다.
     * (여기서의 사용자는 서비스 사용자(고객)를 의미합니다.)
     *
     * @param userId 수정할 사용자 ID
     * @param request 수정할 사용자 정보 (이름, 주소, 전화번호)
     * @return 수정된 사용자 정보 UserResponse
     * @throws NoSuchElementException 해당 ID의 사용자를 찾을 수 없을 경우
     */
    @Transactional
    public UserResponse updateUser(Integer userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.updateProfile(request.name(), request.address(), request.phone());

        return UserResponse.from(user);
    }

    /**
     * 관리자 - 지정된 기간 동안의 일별/월별 판매액 통계를 조회합니다.
     *
     * @param startDate 통계 시작일
     * @param endDate 통계 종료일
     * @return 일별 판매액 통계 목록
     */
    public List<SalesStatisticsResponseDto> getDailySalesStatistics(LocalDate startDate, LocalDate endDate) {
        List<Order> allOrders = orderRepository.findByOrderDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        return allOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().toLocalDate(),
                        Collectors.summingLong(Order::getTotalPrice)
                ))
                .entrySet().stream()
                .map(entry -> new SalesStatisticsResponseDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SalesStatisticsResponseDto::date))
                .collect(Collectors.toList());
    }

    /**
     * 관리자 - 상품별 총 판매량 및 총 판매액 통계를 조회합니다.
     *
     * @return 상품별 판매 통계 목록
     */
    public List<ProductSalesStatisticsResponseDto> getProductSalesStatistics() {
        List<OrderItem> allOrderItems = orderRepository.findAllOrderItemsWithProduct();

        return allOrderItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProduct,
                        Collectors.reducing(
                                new ProductSalesStatisticsResponseDto(null, null, 0L, 0L),
                                orderItem -> new ProductSalesStatisticsResponseDto(
                                        orderItem.getProduct().getId(),
                                        orderItem.getProduct().getName(),
                                        (long) orderItem.getQuantity(),
                                        (long) orderItem.getQuantity() * orderItem.getUnitPrice()
                                ),
                                (acc, item) -> new ProductSalesStatisticsResponseDto(
                                        item.productId(),
                                        item.productName(),
                                        acc.totalQuantitySold() + item.totalQuantitySold(),
                                        acc.totalSalesAmount() + item.totalSalesAmount()
                                )
                        )
                ))
                .entrySet().stream()
                .map(entry -> new ProductSalesStatisticsResponseDto(
                        entry.getKey().getId(),
                        entry.getKey().getName(),
                        entry.getValue().totalQuantitySold(),
                        entry.getValue().totalSalesAmount()
                ))
                .sorted((p1, p2) -> Long.compare(p2.totalSalesAmount(), p1.totalSalesAmount()))
                .collect(Collectors.toList());
    }
}