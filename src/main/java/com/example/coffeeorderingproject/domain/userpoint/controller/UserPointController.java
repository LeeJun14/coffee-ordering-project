package com.example.coffeeorderingproject.domain.userpoint.controller;

import com.example.coffeeorderingproject.domain.userpoint.dto.UserPointChargeRequest;
import com.example.coffeeorderingproject.domain.userpoint.dto.UserPointResponse;
import com.example.coffeeorderingproject.domain.userpoint.entity.UserPoint;
import com.example.coffeeorderingproject.domain.userpoint.service.UserPointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserPointController {

    private final UserPointService userPointService;

    @PatchMapping("/{userId}/point/charge")
    public ResponseEntity<UserPointResponse> charge(
            @PathVariable Long userId,
            @Valid @RequestBody UserPointChargeRequest request
    ) {
        UserPoint userPoint = userPointService.charge(userId, request.amount());
        return ResponseEntity.ok(UserPointResponse.from(userPoint));
    }
}