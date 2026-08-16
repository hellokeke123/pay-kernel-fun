package com.paykernelfun.launcher.infra.compensation;

import com.paykernelfun.core.compensation.TradeQueryCompensationQueue;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * 用 Redis ZSet 实现补偿查询队列：score = 下一次检查时间的毫秒时间戳，member = orderNo。
 * {@link #pollOneDue} 每次只取 score 最小且到期的那一条，取出后立即 remove——ZSet 单元素 remove
 * 本身是原子操作，多个定时任务实例并发轮询时，同一个 orderNo 只会被其中一个成功取走，不会重复处理。
 */
@Component
public class RedisTradeQueryCompensationQueue implements TradeQueryCompensationQueue {

    private static final String KEY = "pay-kernel-fun:compensation:query";

    private final RScoredSortedSet<String> zset;

    public RedisTradeQueryCompensationQueue(RedissonClient redissonClient) {
        this.zset = redissonClient.getScoredSortedSet(KEY);
    }

    @Override
    public void upsert(String orderNo, Date nextCheckAt) {
        zset.add(nextCheckAt.getTime(), orderNo);
    }

    @Override
    public void remove(String orderNo) {
        zset.remove(orderNo);
    }

    @Override
    public Optional<String> pollOneDue(Date now) {
        Collection<String> head = zset.valueRange(0, true, now.getTime(), true, 0, 1);
        for (String orderNo : head) {
            if (zset.remove(orderNo)) {
                return Optional.of(orderNo);
            }
        }
        return Optional.empty();
    }
}
