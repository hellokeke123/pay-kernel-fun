package com.paykernelfun.core.spi.biz;

/** 退款结果的业务回调，按 {@link #bizType()} 路由。onRefundRevoked 对应渠道侧退款被撤销/失败的场景。 */
public interface TradeRefundListener {
    String bizType();

    void onRefundSuccess(String orderNo);

    void onRefundRevoked(String orderNo);
}
