package com.example.coffeeorderingproject.domain.menu.service;

import com.example.coffeeorderingproject.domain.menu.entity.Menu;
import com.example.coffeeorderingproject.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PopularMenuService {

    private final StringRedisTemplate redisTemplate;
    private final MenuRepository menuRepository;

    private static final String POPULAR_MENU_KEY = "popular:menu";
    private static final int TOP_COUNT = 3;

    public void incrementMenuScore(Long menuId) {
        redisTemplate.opsForZSet().incrementScore(POPULAR_MENU_KEY, String.valueOf(menuId), 1);
    }

    @Transactional(readOnly = true)
    public List<Menu> findPopularMenus() {
        Set<String> menuIds = redisTemplate.opsForZSet()
                .reverseRange(POPULAR_MENU_KEY, 0, TOP_COUNT - 1);

        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }

        return menuIds.stream()
                .map(id -> menuRepository.findById(Long.valueOf(id))
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다.")))
                .toList();
    }
}
