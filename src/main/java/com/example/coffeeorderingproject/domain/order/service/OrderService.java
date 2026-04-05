package com.example.coffeeorderingproject.domain.order.service;

import com.example.coffeeorderingproject.common.exception.BusinessException;
import com.example.coffeeorderingproject.common.lock.LockExecutor;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final UserPointRepository userPointRepository;
    private final LockExecutor lockExecutor;
    private final ApplicationEventPublisher eventPublisher;
    private final DataPlatformClient dataPlatformClient;

    public Order order(Long userId, List<OrderItemRequest> orderItemRequests) {
        return lockExecutor.execute("lock:userpoint:" + userId, () -> {
            User user = findUser(userId);
            List<OrderItemInfo> orderItemInfos = resolveOrderItems(orderItemRequests);
            long totalAmount = calculateTotalAmount(orderItemInfos);

            deductPoint(userId, totalAmount);

            Order order = orderRepository.save(Order.create(user, totalAmount));
            saveOrderItems(order, orderItemInfos);
            sendToPlatform(userId, orderItemInfos);
            publishEvents(orderItemInfos);

            return order;
        });
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));
    }

    private List<OrderItemInfo> resolveOrderItems(List<OrderItemRequest> requests) {
        List<OrderItemInfo> orderItemInfos = new ArrayList<>();
        for (OrderItemRequest request : requests) {
            Menu menu = menuRepository.findById(request.menuId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 메뉴입니다."));
            orderItemInfos.add(new OrderItemInfo(menu, request.quantity()));
        }
        return orderItemInfos;
    }

    private long calculateTotalAmount(List<OrderItemInfo> orderItemInfos) {
        return orderItemInfos.stream()
                .mapToLong(info -> info.menu().getPrice() * info.quantity())
                .sum();
    }

    private void deductPoint(Long userId, long totalAmount) {
        UserPoint userPoint = userPointRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "포인트 정보를 찾을 수 없습니다."));
        userPoint.deduct(totalAmount);
    }

    private void saveOrderItems(Order order, List<OrderItemInfo> orderItemInfos) {
        List<OrderItem> orderItems = orderItemInfos.stream()
                .map(info -> OrderItem.create(order, info.menu().getMenuId(), info.menu().getPrice(), info.quantity()))
                .toList();
        orderItemRepository.saveAll(orderItems);
    }

    private void sendToPlatform(Long userId, List<OrderItemInfo> orderItemInfos) {
        orderItemInfos.forEach(info ->
                dataPlatformClient.send(userId, info.menu().getMenuId(), info.menu().getPrice() * info.quantity())
        );
    }

    private void publishEvents(List<OrderItemInfo> orderItemInfos) {
        orderItemInfos.forEach(info ->
                eventPublisher.publishEvent(new OrderCompletedEvent(info.menu().getMenuId()))
        );
    }

    private record OrderItemInfo(Menu menu, Integer quantity) {}
}