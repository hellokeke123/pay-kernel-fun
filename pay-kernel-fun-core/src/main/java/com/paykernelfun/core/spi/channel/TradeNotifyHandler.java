package com.paykernelfun.core.spi.channel;

import com.paykernelfun.core.model.RawNotification;

/** 处理渠道异步回调：验签、CAS 更新订单状态、路由到 {@link com.paykernelfun.core.spi.biz.TradeSuccessListener}。 */
public interface TradeNotifyHandler {
    String channel();

    void handleNotify(RawNotification notification);
}
