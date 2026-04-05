package com.example.coffeeorderingproject.domain.order.service;

import com.example.coffeeorderingproject.common.platform.DataPlatformClient;
import com.example.coffeeorderingproject.domain.menu.entity.Menu;
import com.example.coffeeorderingproject.domain.menu.repository.MenuRepository;
import com.example.coffeeorderingproject.domain.order.entity.Order;
import com.example.coffeeorderingproject.domain.order.event.OrderCompletedEvent;
import com.example.coffeeorderingproject.domain.order.repository.OrderRepository;
import com.example.coffeeorderingproject.domain.orderitem.dto.OrderItemRequest;
import com.example.coffeeorderingproject.domain.orderitem.entity.OrderItem;
import com.example.coffeeorderingproject.domain.orderitem.repository.OrderItemRepository;
import com.example.coffeeorderingproject.domain.user.entity.User;
import com.example.coffeeorderingproject.domain.user.repository.UserRepository;
import com.example.coffeeorderingproject.domain.userpoint.entity.UserPoint;
import com.example.coffeeorderingproject.domain.userpoint.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final UserPointRepository userPointRepository;
    private final RedissonClient redissonClient;
    private final ApplicationEventPublisher eventPublisher;
    private final DataPlatformClient dataPlatformClient;

    @Transactional
    public Order order(Long userId, List<OrderItemRequest> orderItemRequests) {
        String lockKey = "lock:userpoint:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                throw new IllegalStateException("주문 처리 중 다른 요청이 처리 중입니다.");
            }

            // 1. 유저 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            // 2. 메뉴 조회 및 총 금액 계산
            long totalAmount = 0L;
            List<OrderItemInfo> orderItemInfos = new ArrayList<>();

            for (OrderItemRequest request : orderItemRequests) {
                Menu menu = menuRepository.findById(request.menuId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));
                long itemAmount = menu.getPrice() * request.quantity();
                totalAmount += itemAmount;
                orderItemInfos.add(new OrderItemInfo(menu, request.quantity()));
            }

            // 3. 포인트 차감
            UserPoint userPoint = userPointRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("포인트 정보를 찾을 수 없습니다."));
            userPoint.deduct(totalAmount);

            // 4. 주문 저장
            Order order = orderRepository.save(Order.create(user, totalAmount));

            // 5. 주문 아이템 저장
            List<OrderItem> orderItems = orderItemInfos.stream()
                    .map(info -> OrderItem.create(
                            order,
                            info.menu().getMenuId(),
                            info.menu().getPrice(),
                            info.quantity()
                    ))
                    .toList();
            orderItemRepository.saveAll(orderItems);

            // 6. 데이터 플랫폼 전송
            orderItemInfos.forEach(info ->
                    dataPlatformClient.send(userId, info.menu().getMenuId(), info.menu().getPrice() * info.quantity())
            );

            // 7. 이벤트 발행 (커밋 후 Redis 인기 메뉴 업데이트)
            orderItemInfos.forEach(info ->
                    eventPublisher.publishEvent(new OrderCompletedEvent(info.menu().getMenuId()))
            );

            return order;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("주문 처리 중 인터럽트가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private record OrderItemInfo(Menu menu, Integer quantity) {}
}
