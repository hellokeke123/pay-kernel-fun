package com.paykernelfun.launcher.infra.trade;

import com.paykernelfun.core.compensation.TradeCloseCompensationQueue;
import com.paykernelfun.core.compensation.TradeQueryCompensationQueue;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.registry.ChannelRegistry;
import com.paykernelfun.core.spi.biz.TradeCloseListener;
import com.paykernelfun.core.spi.channel.TradeCloseExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "关闭订单"这件事只应该有一处实现：调渠道关单 -> 清理两个补偿队列 -> 按 bizType 路由业务回调。
 * 手动关单接口（{@code TradeController#close}）和到期关单定时任务（{@code TradeCloseCompensationTask}）
 * 都是"订单该被关闭"的触发源，收尾逻辑必须共用这一份，否则两条路径分别维护迟早会走样——
 * 与支付成功收尾（{@link TradePaymentConfirmer}）是完全对称的设计。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradeOrderCloser {

    private final ChannelRegistry<TradeCloseExecutor> tradeCloseExecutorRegistry;
    private final ChannelRegistry<TradeCloseListener> tradeCloseListenerRegistry;
    private final TradeQueryCompensationQueue queryCompensationQueue;
    private final TradeCloseCompensationQueue closeCompensationQueue;

    /** @return true 表示渠道侧关单成功（内部已 CAS 翻转为 CLOSED）；false 表示渠道侧关单失败，订单状态未变 */
    public boolean close(TradeOrder order) {
        boolean closed = tradeCloseExecutorRegistry.require(order.getChannel()).close(order);
        if (closed) {
            tradeCloseListenerRegistry.find(order.getBizType())
                    .ifPresent(listener -> listener.onOrderClosed(order.getOrderNo()));
            queryCompensationQueue.remove(order.getOrderNo());
            closeCompensationQueue.remove(order.getOrderNo());
        } else {
            log.warn("[pay-kernel-fun] close order failed on channel side, orderNo={}", order.getOrderNo());
        }
        return closed;
    }
}
