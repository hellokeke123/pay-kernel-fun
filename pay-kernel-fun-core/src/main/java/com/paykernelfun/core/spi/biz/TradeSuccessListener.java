package com.paykernelfun.core.spi.biz;

/**
 * 支付成功后的业务回调，按 {@link #bizType()} 路由。渠道实现（{@code TradeNotifyHandler} 等）只负责翻转订单状态，
 * 不感知具体业务，业务方实现该接口来响应"该做什么"（发货、发放权益等）。
 */
public interface TradeSuccessListener {
    String bizType();

    void onPaySuccess(String bizOrderNo, String orderNo);
}
