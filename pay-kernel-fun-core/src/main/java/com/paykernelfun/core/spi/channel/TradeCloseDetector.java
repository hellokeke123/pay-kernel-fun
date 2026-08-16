package com.paykernelfun.core.spi.channel;

/**
 * 面向没有可靠关单 API/回调的渠道的扩展点：反向探测某笔订单在渠道侧是否已经失效/关闭，
 * 供关单补偿任务判断本地超时订单能否安全标记为已关闭。支付宝案例不实现该接口（支付宝有标准关单 API，见 {@link TradeCloseExecutor}）。
 */
public interface TradeCloseDetector {
    String channel();

    boolean isClosedOnChannel(String orderNo);
}
