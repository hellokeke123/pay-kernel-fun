package com.paykernelfun.subscription.spi;

import com.paykernelfun.subscription.model.Subscription;

import java.util.List;

/** 恢复购买（App 换设备/重装后找回已购订阅），部分渠道（Apple/Google）要求支持。 */
public interface SubscriptionRestoreHandler {
    String channel();

    List<Subscription> restore(String bizUserId);
}
