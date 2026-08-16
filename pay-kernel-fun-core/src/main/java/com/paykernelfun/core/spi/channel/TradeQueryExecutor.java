package com.paykernelfun.core.spi.channel;

import com.paykernelfun.core.model.TradeQueryDetail;

/** 主动向渠道查询交易状态，用于补偿回调丢失的场景。 */
public interface TradeQueryExecutor {
    String channel();

    boolean isPaid(String orderNo);

    TradeQueryDetail queryDetail(String orderNo);
}
