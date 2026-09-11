package com.paykernelfun.subscription.spi;

import com.paykernelfun.subscription.model.Subscription;
import com.paykernelfun.core.model.RawNotification;

/** 订阅渠道 SPI：发起订阅购买/验单，处理渠道续期/失效回调。 */
public interface SubscriptionChannelHandler {
    String channel();

    Subscription purchase(String bizUserId, String productId);

    void handleNotify(RawNotification notification);
}
