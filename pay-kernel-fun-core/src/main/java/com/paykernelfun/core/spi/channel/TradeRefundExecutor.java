package com.paykernelfun.core.spi.channel;

import java.math.BigDecimal;

/** 向渠道发起退款。同步退款渠道可在返回前完成状态收尾；异步退款渠道只受理，最终状态由 {@link TradeNotifyHandler} 或后续回调收尾。 */
public interface TradeRefundExecutor {
    String channel();

    String refund(String orderNo, BigDecimal amount, String reason);
}
