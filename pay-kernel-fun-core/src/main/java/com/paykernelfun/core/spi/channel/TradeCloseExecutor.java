package com.paykernelfun.core.spi.channel;

import com.paykernelfun.core.model.TradeOrder;

/** 主动关闭渠道侧未支付订单（如超时未支付场景）。 */
public interface TradeCloseExecutor {
    String channel();

    boolean close(TradeOrder order);
}
