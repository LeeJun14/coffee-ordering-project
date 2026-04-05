package com.example.coffeeorderingproject.domain.userpoint.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserPointChargeRequest(
        @NotNull(message = "충전 금액은 필수입니다.")
        @Min(value = 1, message = "충전 금액은 1 이상이어야 합니다.")
        Long amount
) {}