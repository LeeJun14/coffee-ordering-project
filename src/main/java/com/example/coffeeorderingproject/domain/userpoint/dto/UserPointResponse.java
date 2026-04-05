package com.example.coffeeorderingproject.domain.userpoint.dto;

import com.example.coffeeorderingproject.domain.userpoint.entity.UserPoint;

public record UserPointResponse(
        Long userId,
        Long balance
) {
    public static UserPointResponse from(UserPoint userPoint) {
        return new UserPointResponse(userPoint.getUser().getUserId(), userPoint.getBalance());
    }
}