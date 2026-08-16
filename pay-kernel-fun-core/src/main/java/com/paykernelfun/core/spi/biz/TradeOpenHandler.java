package com.paykernelfun.core.spi.biz;

import com.paykernelfun.core.model.TradeOpenRequest;
import com.paykernelfun.core.model.TradeOrder;

/** 业务方下单入口：把自己的下单请求转成一笔 {@link TradeOrder}，按 {@link #bizType()} 路由。 */
public interface TradeOpenHandler {
    String bizType();

    TradeOrder buildOrder(TradeOpenRequest request);
}
