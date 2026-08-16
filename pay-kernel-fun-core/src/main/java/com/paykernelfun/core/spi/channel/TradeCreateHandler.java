package com.paykernelfun.core.spi.channel;

import com.paykernelfun.core.model.TradeCreateRequest;
import com.paykernelfun.core.model.TradeCreateResult;

/** 向渠道发起下单。每个渠道实现一个 Bean，按 {@link #channel()} 注册到 ChannelRegistry。 */
public interface TradeCreateHandler {
    String channel();

    TradeCreateResult create(TradeCreateRequest request);
}
