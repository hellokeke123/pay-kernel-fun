package com.paykernelfun.launcher.infra.trade;

import com.paykernelfun.core.compensation.TradeCloseCompensationQueue;
import com.paykernelfun.core.compensation.TradeQueryCompensationQueue;
import com.paykernelfun.core.lock.TradeLock;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.registry.ChannelRegistry;
import com.paykernelfun.core.repository.TradeOrderRepository;
import com.paykernelfun.core.spi.biz.TradeSuccessListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * "确认支付成功"这件事只应该有一处实现：加锁 -> CAS 翻转订单状态 -> 按 bizType 路由业务回调 -> 从两个补偿队列移除。
 * 支付宝异步回调（{@code AlipayNotifyHandler}）和补偿查询任务（{@code TradeQueryCompensationTask}）
 * 都是"发现订单支付成功"的触发源，但收尾逻辑必须共用这一份，否则两条路径分别维护迟早会走样。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradePaymentConfirmer {

    private static final String LOCK_KEY_PREFIX = "pay-kernel-fun:lock:trade:";
    private static final long LOCK_LEASE_MILLIS = 5000L;

    private final TradeOrderRepository tradeOrderRepository;
    private final TradeLock tradeLock;
    private final ChannelRegistry<TradeSuccessListener> tradeSuccessListenerRegistry;
    private final TradeQueryCompensationQueue queryCompensationQueue;
    private final TradeCloseCompensationQueue closeCompensationQueue;

    /** @return true 表示本次调用完成了状态翻转；false 表示拿不到锁或订单已经不是 PENDING（重复触发，安全忽略） */
    public boolean confirmPaid(String orderNo, Date payTime) {
        return tradeLock.executeIfLocked(LOCK_KEY_PREFIX + orderNo, LOCK_LEASE_MILLIS, () -> {
            TradeOrder order = tradeOrderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new IllegalStateException("order not found: " + orderNo));
            boolean marked = tradeOrderRepository.casMarkPaid(orderNo, payTime);
            if (marked) {
                tradeSuccessListenerRegistry.find(order.getBizType())
                        .ifPresent(listener -> listener.onPaySuccess(order.getBizOrderNo(), orderNo));
                queryCompensationQueue.remove(orderNo);
                closeCompensationQueue.remove(orderNo);
            } else {
                log.info("[pay-kernel-fun] duplicate pay confirmation ignored, orderNo={}", orderNo);
            }
            return marked;
        }).orElse(false);
    }
}
