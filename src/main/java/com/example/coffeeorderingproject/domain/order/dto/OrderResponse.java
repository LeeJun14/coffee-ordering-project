package com.example.coffeeorderingproject.domain.order.dto;

import com.example.coffeeorderingproject.domain.order.entity.Order;

import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        Long userId,
        Long totalAmount,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getUser().getUserId(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}