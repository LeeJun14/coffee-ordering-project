package com.example.coffeeorderingproject.common.lock;

import com.example.coffeeorderingproject.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class LockExecutor {

    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private static final long WAIT_TIME = 5L;
    private static final long LEASE_TIME = 3L;

    public <T> T execute(String lockKey, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)) {
                throw new BusinessException(HttpStatus.CONFLICT, "현재 다른 요청이 처리 중입니다.");
            }
            return transactionTemplate.execute(status -> supplier.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "처리 중 인터럽트가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}