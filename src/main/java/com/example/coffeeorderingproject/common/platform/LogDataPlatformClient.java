package com.example.coffeeorderingproject.common.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogDataPlatformClient implements DataPlatformClient {

    @Override
    public void send(Long userId, Long menuId, Long amount) {
        log.info("[데이터 플랫폼 전송] userId={}, menuId={}, amount={}", userId, menuId, amount);
    }
}
