package com.example.coffeeorderingproject.domain.menu.controller;

import com.example.coffeeorderingproject.domain.menu.dto.MenuResponse;
import com.example.coffeeorderingproject.domain.menu.service.MenuService;
import com.example.coffeeorderingproject.domain.menu.service.PopularMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final PopularMenuService popularMenuService;

    @GetMapping
    public ResponseEntity<List<MenuResponse>> getMenus() {
        List<MenuResponse> response = menuService.findAll().stream()
                .map(MenuResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MenuResponse>> getPopularMenus() {
        List<MenuResponse> response = popularMenuService.findPopularMenus().stream()
                .map(MenuResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
