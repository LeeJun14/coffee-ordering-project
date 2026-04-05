package com.example.coffeeorderingproject.domain.userpoint.service;

import com.example.coffeeorderingproject.common.exception.BusinessException;
import com.example.coffeeorderingproject.common.lock.LockExecutor;
import com.example.coffeeorderingproject.domain.user.repository.UserRepository;
import com.example.coffeeorderingproject.domain.userpoint.entity.UserPoint;
import com.example.coffeeorderingproject.domain.userpoint.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPointService {

    private final UserPointRepository userPointRepository;
    private final UserRepository userRepository;
    private final LockExecutor lockExecutor;

    public UserPoint charge(Long userId, Long amount) {
        return lockExecutor.execute("lock:userpoint:" + userId, () -> {

            userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));

            UserPoint userPoint = userPointRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "포인트 정보를 찾을 수 없습니다."));

            userPoint.charge(amount);
            return userPoint;
        });
    }
}