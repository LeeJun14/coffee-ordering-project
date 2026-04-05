package com.example.coffeeorderingproject.domain.userpoint.service;

import com.example.coffeeorderingproject.domain.user.entity.User;
import com.example.coffeeorderingproject.domain.user.repository.UserRepository;
import com.example.coffeeorderingproject.domain.userpoint.entity.UserPoint;
import com.example.coffeeorderingproject.domain.userpoint.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserPointService {

    private final UserPointRepository userPointRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

    @Transactional
    public UserPoint charge(Long userId, Long amount) {
        String lockKey = "lock:userpoint:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                throw new IllegalStateException("포인트 충전 중 다른 요청이 처리 중입니다.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            UserPoint userPoint = userPointRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("포인트 정보를 찾을 수 없습니다."));

            userPoint.charge(amount);
            return userPoint;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("포인트 충전 중 인터럽트가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
