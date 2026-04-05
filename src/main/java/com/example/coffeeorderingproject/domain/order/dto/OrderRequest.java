package com.example.coffeeorderingproject.domain.order.dto;

import com.example.coffeeorderingproject.domain.orderitem.dto.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        @NotNull(message = "유저 ID는 필수입니다.")
        Long userId,

        @NotEmpty(message = "주문 항목은 필수입니다.")
        @Valid
        List<OrderItemRequest> orderItems
) {}
