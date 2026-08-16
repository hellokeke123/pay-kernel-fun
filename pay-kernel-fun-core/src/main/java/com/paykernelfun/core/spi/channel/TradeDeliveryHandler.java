package com.paykernelfun.core.spi.channel;

/**
 * 面向要求商户显式确认履约的渠道的扩展点（如微信小程序虚拟支付要求支付成功后调用"发货确认"接口）。
 * 支付宝没有这一步，支付宝案例不实现该接口。
 */
public interface TradeDeliveryHandler {
    String channel();

    void deliver(String orderNo);
}
