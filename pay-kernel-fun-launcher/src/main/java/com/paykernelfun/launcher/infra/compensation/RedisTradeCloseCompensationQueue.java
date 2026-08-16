package com.paykernelfun.launcher.infra.compensation;

import com.paykernelfun.core.compensation.TradeCloseCompensationQueue;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/** 用 Redis ZSet 实现到期关单队列，一次只取一条并原子移除，语义与 {@link RedisTradeQueryCompensationQueue} 一致。 */
@Component
public class RedisTradeCloseCompensationQueue implements TradeCloseCompensationQueue {

    private static final String KEY = "pay-kernel-fun:compensation:close";

    private final RScoredSortedSet<String> zset;

    public RedisTradeCloseCompensationQueue(RedissonClient redissonClient) {
        this.zset = redissonClient.getScoredSortedSet(KEY);
    }

    @Override
    public void upsert(String orderNo, Date expireTime) {
        zset.add(expireTime.getTime(), orderNo);
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
