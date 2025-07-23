package com.back.global.scheduler;

import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.repository.OrderRepository;
import com.back.global.service.MailService;
import com.back.global.service.MockMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyOrderScheduler {
    
    private final OrderRepository orderRepository;
    
    @Autowired(required = false)
    private MailService mailService;
    
    @Autowired(required = false)  
    private MockMailService mockMailService;
    
    // 매일 오후 2시에 실행 (cron: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 14 * * *")
    @Transactional
    public void processAndSendDailyOrders() {
        log.info("Starting daily order processing at {}", LocalDateTime.now());
        
        try {
            // 전날 오후 2시부터 오늘 오후 2시까지의 '배송준비중' 주문 조회
            LocalDateTime endTime = LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime startTime = endTime.minusDays(1);
            
            List<Order> ordersToProcess = orderRepository.findByOrderDateBetweenAndStatus(
                startTime, endTime, OrderStatus.배송준비중
            );
            
            if (ordersToProcess.isEmpty()) {
                log.info("No orders to process for period {} to {}", startTime, endTime);
                return;
            }
            
            // 이메일별로 주문 그룹화
            Map<String, List<Order>> ordersByEmail = ordersToProcess.stream()
                .collect(Collectors.groupingBy(order -> order.getUser().getEmail()));
            
            log.info("Processing {} orders for {} unique customers", ordersToProcess.size(), ordersByEmail.size());
            
            // 각 이메일별로 통합 주문서 생성 및 발송
            for (Map.Entry<String, List<Order>> entry : ordersByEmail.entrySet()) {
                String email = entry.getKey();
                List<Order> userOrders = entry.getValue();
                
                try {
                    sendConsolidatedOrderEmail(email, userOrders);
                    
                    // 주문 상태를 '배송중'으로 변경
                    userOrders.forEach(order -> order.setStatus(OrderStatus.배송중));
                    
                    log.info("Successfully processed {} orders for {}", userOrders.size(), email);
                } catch (Exception e) {
                    log.error("Failed to process orders for {}: {}", email, e.getMessage(), e);
                }
            }
            
            log.info("Daily order processing completed successfully");
            
        } catch (Exception e) {
            log.error("Error during daily order processing: {}", e.getMessage(), e);
        }
    }
    
    private void sendConsolidatedOrderEmail(String email, List<Order> orders) {
        // 첫 번째 주문에서 사용자 정보 추출
        Order firstOrder = orders.get(0);
        String userName = firstOrder.getUser().getName();
        String address = firstOrder.getAddress();
        
        // 총 주문 건수
        int totalOrderCount = orders.size();
        
        // 모든 주문의 총 금액 계산
        int totalAmount = orders.stream()
            .mapToInt(Order::getTotalPrice)
            .sum();
        
        // 주문 내역 문자열 생성
        StringBuilder orderDetails = new StringBuilder();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                orderDetails.append(String.format("  - %s × %d개 = %,d원\n", 
                    item.getProduct().getName(), 
                    item.getQuantity(), 
                    item.getTotalPrice()));
            }
        }
        
        // 첫 번째 주문 번호를 대표 주문 번호로 사용
        Integer representativeOrderId = firstOrder.getId();
        
        // 이메일 내용 생성
        String emailBody = String.format("""
            안녕하세요, %s님!
            
            Grids & Circles에서 오늘의 주문 내역을 안내드립니다.
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            📦 통합 주문 명세서
            주문번호: #%d (총 %d건)
            
            [주문 내역]
            %s
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            총 주문 금액: %,d원
            배송 주소: %s
            
            신선한 원두로 행복한 하루 되세요!
            
            Grids & Circles
            """, 
            userName, 
            representativeOrderId, 
            totalOrderCount,
            orderDetails.toString(),
            totalAmount, 
            address
        );
        
        String subject = String.format("[Grids & Circles] %s님의 오늘 주문 내역 (#%d)", userName, representativeOrderId);
        
        // 메일 서비스 우선순위: MockMailService > MailService
        try {
            if (mockMailService != null) {
                log.info("Using MockMailService for email to: {}", email);
                mockMailService.sendSummaryMail(email, subject, emailBody);
            } else if (mailService != null) {
                log.info("Using MailService for email to: {}", email);
                mailService.sendSummaryMail(email, subject, emailBody);
            } else {
                log.warn("No mail service available - email not sent to: {}", email);
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage(), e);
            throw e; // 이메일 실패시 상위로 전파
        }
    }
}
