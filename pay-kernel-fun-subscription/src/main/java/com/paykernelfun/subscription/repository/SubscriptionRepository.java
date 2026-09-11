package com.paykernelfun.subscription.repository;

import com.paykernelfun.subscription.model.Subscription;

import java.util.List;
import java.util.Optional;

/** 订阅持久化 SPI，语义与 {@code TradeOrderRepository}（core）平行但不复用，字段/生命周期不同。 */
public interface SubscriptionRepository {

    void save(Subscription subscription);

    Optional<Subscription> findBySubscriptionNo(String subscriptionNo);

    List<Subscription> findActiveByBizUserId(String bizUserId);
}
