package com.paykernelfun.launcher.infra.lock;

import com.paykernelfun.core.lock.TradeLock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** {@link TradeLock} 的 Redisson 实现，{@code RLock} 本身就是 Redlock 算法的实现。 */
@Component
@RequiredArgsConstructor
public class RedissonTradeLock implements TradeLock {

    private final RedissonClient redissonClient;

    @Override
    public <T> Optional<T> executeIfLocked(String key, long leaseMillis, Supplier<T> action) {
        RLock lock = redissonClient.getLock(key);
        boolean locked;
        try {
            locked = lock.tryLock(0, leaseMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        if (!locked) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(action.get());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
