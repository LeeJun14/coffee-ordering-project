package com.example.coffeeorderingproject.domain.menu.service;

import com.example.coffeeorderingproject.domain.menu.entity.Menu;
import com.example.coffeeorderingproject.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PopularMenuService {

    private final StringRedisTemplate redisTemplate;
    private final MenuRepository menuRepository;

    private static final String POPULAR_MENU_KEY_PREFIX = "popular:menu:";
    private static final String UNION_KEY = "popular:menu:union:temp";
    private static final int TOP_COUNT = 3;
    private static final int PERIOD_DAYS = 7;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void incrementMenuScore(Long menuId) {
        String todayKey = todayKey();
        redisTemplate.opsForZSet().incrementScore(todayKey, String.valueOf(menuId), 1);
        redisTemplate.expire(todayKey, PERIOD_DAYS + 1, TimeUnit.DAYS);
    }

    @Transactional(readOnly = true)
    public List<Menu> findPopularMenus() {
        List<String> keys = last7DaysKeys();
        redisTemplate.opsForZSet().unionAndStore(keys.get(0), keys.subList(1, keys.size()), UNION_KEY);
        redisTemplate.expire(UNION_KEY, 1, TimeUnit.MINUTES);

        Set<String> menuIds = redisTemplate.opsForZSet().reverseRange(UNION_KEY, 0, TOP_COUNT - 1);

        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }

        List<Long> ids = menuIds.stream().map(Long::valueOf).toList();
        return menuRepository.findAllById(ids);
    }

    private String todayKey() {
        return POPULAR_MENU_KEY_PREFIX + LocalDate.now().format(FORMATTER);
    }

    private List<String> last7DaysKeys() {
        return IntStream.range(0, PERIOD_DAYS)
                .mapToObj(i -> POPULAR_MENU_KEY_PREFIX + LocalDate.now().minusDays(i).format(FORMATTER))
                .toList();
    }
}