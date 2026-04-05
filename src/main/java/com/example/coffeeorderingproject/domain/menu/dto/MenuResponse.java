package com.example.coffeeorderingproject.domain.menu.dto;

import com.example.coffeeorderingproject.domain.menu.entity.Menu;

public record MenuResponse(
        Long menuId,
        String name,
        Long price
) {
    public static MenuResponse from(Menu menu) {
        return new MenuResponse(menu.getMenuId(), menu.getName(), menu.getPrice());
    }
}
