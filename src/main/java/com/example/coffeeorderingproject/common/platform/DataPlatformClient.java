package com.example.coffeeorderingproject.common.platform;

public interface DataPlatformClient {
    void send(Long userId, Long menuId, Long amount);
}