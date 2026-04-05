package com.example.coffeeorderingproject.domain.order.service;

import com.example.coffeeorderingproject.common.exception.BusinessException;
import com.example.coffeeorderingproject.common.lock.LockExecutor;
import com.example.coffeeorderingproject.common.platform.DataPlatformClient;
import com.example.coffeeorderingproject.domain.menu.entity.Menu;
import com.example.coffeeorderingproject.domain.menu.repository.MenuRepository;
import com.example.coffeeorderingproject.domain.order.entity.Order;
import com.example.coffeeorderingproject.domain.order.repository.OrderRepository;
import com.example.coffeeorderingproject.domain.orderitem.dto.OrderItemRequest;
import com.example.coffeeorderingproject.domain.orderitem.repository.OrderItemRepository;
import com.example.coffeeorderingproject.domain.user.entity.User;
import com.example.coffeeorderingproject.domain.user.repository.UserRepository;
import com.example.coffeeorderingproject.domain.userpoint.entity.UserPoint;
import com.example.coffeeorderingproject.domain.userpoint.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private UserPointRepository userPointRepository;
    @Mock private LockExecutor lockExecutor;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private DataPlatformClient dataPlatformClient;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Menu menu;
    private UserPoint userPoint;

    @BeforeEach
    void setUp() {
        given(lockExecutor.execute(anyString(), any(Supplier.class))).willAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        user = User.create("테스터");
        menu = Menu.create("아메리카노", 4000L);
        userPoint = UserPoint.create(user);
        userPoint.charge(50000L);
    }

    @Test
    void 정상적으로_주문한다() {
        // given
        List<OrderItemRequest> requests = List.of(new OrderItemRequest(1L, 2));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));
        given(userPointRepository.findByUser_UserId(1L)).willReturn(Optional.of(userPoint));
        given(orderRepository.save(any(Order.class))).willAnswer(i -> i.getArgument(0));

        // when
        Order order = orderService.order(1L, requests);

        // then
        assertThat(order.getTotalAmount()).isEqualTo(8000L);
        assertThat(userPoint.getBalance()).isEqualTo(42000L);
    }

    @Test
    void 존재하지_않는_유저면_예외가_발생한다() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.order(1L, List.of(new OrderItemRequest(1L, 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 유저입니다.");
    }

    @Test
    void 존재하지_않는_메뉴면_예외가_발생한다() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(menuRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.order(1L, List.of(new OrderItemRequest(1L, 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 메뉴입니다.");
    }

    @Test
    void 잔액이_부족하면_예외가_발생한다() {
        // given
        UserPoint emptyPoint = UserPoint.create(user);
        List<OrderItemRequest> requests = List.of(new OrderItemRequest(1L, 1));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));
        given(userPointRepository.findByUser_UserId(1L)).willReturn(Optional.of(emptyPoint));

        // when & then
        assertThatThrownBy(() -> orderService.order(1L, requests))
                .isInstanceOf(BusinessException.class)
                .hasMessage("잔액이 부족합니다.");
    }
}
