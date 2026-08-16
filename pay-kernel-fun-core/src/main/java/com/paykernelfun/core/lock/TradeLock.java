package com.paykernelfun.core.lock;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 分布式锁 SPI，用于保证并发回调/退款处理的幂等性——单纯的数据库 CAS 无法覆盖"同一订单号的两次回调
 * 同时读到 PENDING、都通过 CAS 前置校验"这类窗口期竞争，必须在处理前先加锁。
 * core 不引入 Redis 依赖，接入方自行实现（案例应用用 Redisson 实现，见 pay-kernel-fun-launcher）。
 */
public interface TradeLock {

    /**
     * 尝试加锁并在持锁期间执行 action；拿不到锁直接返回 empty，不阻塞等待、不重试
     * （回调/补偿任务本身会重新触发，没必要在这里排队）。
     */
    <T> Optional<T> executeIfLocked(String key, long leaseMillis, Supplier<T> action);
}
