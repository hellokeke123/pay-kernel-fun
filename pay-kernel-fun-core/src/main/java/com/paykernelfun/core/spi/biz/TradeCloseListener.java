package com.paykernelfun.core.spi.biz;

/**
 * 订单被关闭（超时未支付/主动关单）后的业务回调，按 {@link #bizType()} 路由。
 * 和 {@link TradeSuccessListener}/{@link TradeRefundListener} 三者构成订单生命周期的完整回调集合——
 * 支付成功、退款成功、关单，业务方各自决定要做什么（本项目的示例业务用它来回滚预扣库存）。
 */
public interface TradeCloseListener {
    String bizType();

    void onOrderClosed(String orderNo);
}
