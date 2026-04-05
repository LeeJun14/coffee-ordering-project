package com.example.coffeeorderingproject.domain.userpoint.service;

import com.example.coffeeorderingproject.common.exception.BusinessException;
import com.example.coffeeorderingproject.domain.menu.entity.Menu;
import com.example.coffeeorderingproject.domain.menu.repository.MenuRepository;
import com.example.coffeeorderingproject.domain.order.repository.OrderRepository;
import com.example.coffeeorderingproject.domain.order.service.OrderService;
import com.example.coffeeorderingproject.domain.orderitem.repository.OrderItemRepository;
import com.example.coffeeorderingproject.domain.orderitem.dto.OrderItemRequest;
import com.example.coffeeorderingproject.domain.user.entity.User;
import com.example.coffeeorderingproject.domain.user.repository.UserRepository;
import com.example.coffeeorderingproject.domain.userpoint.entity.UserPoint;
import com.example.coffeeorderingproject.domain.userpoint.repository.UserPointRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserPointConcurrencyTest {

    @Autowired private UserPointService userPointService;
    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserPointRepository userPointRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;

    private Long userId;
    private Long menuId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.create("동시성테스터"));
        userPointRepository.save(UserPoint.create(user));
        userId = user.getUserId();

        Menu menu = menuRepository.save(Menu.create("아메리카노", 1000L));
        menuId = menu.getMenuId();
    }

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userPointRepository.deleteAll();
        userRepository.deleteAll();
        menuRepository.deleteAll();
    }

    @Test
    void 동시에_포인트를_충전해도_잔액이_정확하다() throws InterruptedException {
        // given
        int threadCount = 10;
        long chargeAmount = 1000L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    userPointService.charge(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then - 10번 * 1000원 = 10,000원이어야 한다
        UserPoint result = userPointRepository.findByUser_UserId(userId).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(chargeAmount * threadCount);
    }

    @Test
    void 잔액보다_많은_주문이_동시에_들어오면_잔액은_음수가_되지_않는다() throws InterruptedException {
        // given - 5000원 충전 후 1000원짜리 주문 10개 동시 요청
        userPointService.charge(userId, 5000L);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    orderService.order(userId, List.of(new OrderItemRequest(menuId, 1)));
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then - 잔액은 음수가 되어선 안 되고, 성공한 주문 수만큼만 차감되어야 한다
        UserPoint result = userPointRepository.findByUser_UserId(userId).orElseThrow();
        assertThat(result.getBalance()).isGreaterThanOrEqualTo(0L);
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failCount.get()).isEqualTo(5);
    }
}
