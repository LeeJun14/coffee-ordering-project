package com.example.coffeeorderingproject.domain.order.controller;

import com.example.coffeeorderingproject.domain.order.dto.OrderRequest;
import com.example.coffeeorderingproject.domain.order.dto.OrderResponse;
import com.example.coffeeorderingproject.domain.order.entity.Order;
import com.example.coffeeorderingproject.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> order(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.order(request.userId(), request.orderItems());
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}