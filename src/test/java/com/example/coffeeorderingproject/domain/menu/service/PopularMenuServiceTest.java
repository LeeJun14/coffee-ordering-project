package com.example.coffeeorderingproject.domain.menu.service;

import com.example.coffeeorderingproject.domain.menu.entity.Menu;
import com.example.coffeeorderingproject.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PopularMenuServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private PopularMenuService popularMenuService;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
    }

    @Test
    void 메뉴_주문_시_오늘_날짜_키에_점수를_증가시킨다() {
        // when
        popularMenuService.incrementMenuScore(1L);

        // then
        verify(zSetOperations).incrementScore(contains("popular:menu:"), eq("1"), eq(1.0));
        verify(redisTemplate).expire(contains("popular:menu:"), eq(8L), any());
    }

    @Test
    void 최근_7일_인기_메뉴_TOP3를_조회한다() {
        // given
        Set<String> menuIds = new LinkedHashSet<>(Set.of("1", "2", "3"));
        given(zSetOperations.unionAndStore(anyString(), anyList(), anyString())).willReturn(3L);
        given(zSetOperations.reverseRange(anyString(), eq(0L), eq(2L))).willReturn(menuIds);
        given(menuRepository.findAllById(anyList())).willReturn(List.of(
                Menu.create("아메리카노", 4000L),
                Menu.create("카페라떼", 5000L),
                Menu.create("바닐라라떼", 5500L)
        ));

        // when
        List<Menu> result = popularMenuService.findPopularMenus();

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    void 주문_내역이_없으면_빈_리스트를_반환한다() {
        // given
        given(zSetOperations.unionAndStore(anyString(), anyList(), anyString())).willReturn(0L);
        given(zSetOperations.reverseRange(anyString(), eq(0L), eq(2L))).willReturn(null);

        // when
        List<Menu> result = popularMenuService.findPopularMenus();

        // then
        assertThat(result).isEmpty();
    }
}
