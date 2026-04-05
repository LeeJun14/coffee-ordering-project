package com.example.coffeeorderingproject.domain.userpoint.service;

import com.example.coffeeorderingproject.common.exception.BusinessException;
import com.example.coffeeorderingproject.common.lock.LockExecutor;
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

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserPointServiceTest {

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LockExecutor lockExecutor;

    @InjectMocks
    private UserPointService userPointService;

    @BeforeEach
    void setUp() {
        given(lockExecutor.execute(anyString(), any(Supplier.class))).willAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void 포인트를_충전한다() {
        // given
        User user = User.create("테스터");
        UserPoint userPoint = UserPoint.create(user);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userPointRepository.findByUser_UserId(1L)).willReturn(Optional.of(userPoint));

        // when
        UserPoint result = userPointService.charge(1L, 10000L);

        // then
        assertThat(result.getBalance()).isEqualTo(10000L);
    }

    @Test
    void 존재하지_않는_유저면_예외가_발생한다() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userPointService.charge(1L, 10000L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 유저입니다.");
    }

    @Test
    void 포인트_정보가_없으면_예외가_발생한다() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(User.create("테스터")));
        given(userPointRepository.findByUser_UserId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userPointService.charge(1L, 10000L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("포인트 정보를 찾을 수 없습니다.");
    }

    @Test
    void 충전_금액이_0이하면_예외가_발생한다() {
        // given
        User user = User.create("테스터");
        UserPoint userPoint = UserPoint.create(user);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userPointRepository.findByUser_UserId(1L)).willReturn(Optional.of(userPoint));

        // when & then
        assertThatThrownBy(() -> userPointService.charge(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("충전 금액은 0보다 커야 합니다.");
    }
}
