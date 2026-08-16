package com.paykernelfun.launcher.infra.task;

import com.paykernelfun.core.compensation.TradeQueryCompensationQueue;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.model.TradeQueryDetail;
import com.paykernelfun.core.model.TradeStatus;
import com.paykernelfun.core.registry.ChannelRegistry;
import com.paykernelfun.core.repository.TradeOrderRepository;
import com.paykernelfun.core.spi.channel.TradeQueryExecutor;
import com.paykernelfun.launcher.infra.config.TradeProperties;
import com.paykernelfun.launcher.infra.trade.TradePaymentConfirmer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * 补偿查询任务：弥补渠道异步回调丢失/延迟的场景。
 *
 * 下单时把订单按"有效期截止时间"写入 {@link TradeQueryCompensationQueue}（见 {@code TradeController#createOrder}）；
 * 每次触发一直从队列里取一条到期订单处理，直到取不到为止（不做批量），
 * 主动查渠道支付状态——查到已支付就走 {@link TradePaymentConfirmer} 收尾（同时会清理本队列和关单补偿队列）；
 * 查到未支付，按 {@link TradeProperties#getQueryRetryIntervalSeconds()} 顺延到下一次检查，
 * 除非已经超过订单有效期，那种情况交给 {@code TradeCloseCompensationTask} 处理，本任务不再重新入队。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradeQueryCompensationTask {

    private final TradeQueryCompensationQueue queryCompensationQueue;
    private final TradeOrderRepository tradeOrderRepository;
    private final ChannelRegistry<TradeQueryExecutor> tradeQueryExecutorRegistry;
    private final TradePaymentConfirmer paymentConfirmer;
    private final TradeProperties tradeProperties;

    @Scheduled(fixedDelayString = "${pay-kernel-fun.trade.poll-interval-millis:5000}")
    public void run() {
        Date now = new Date();
        Optional<String> next;
        while ((next = queryCompensationQueue.pollOneDue(now)).isPresent()) {
            String orderNo = next.get();
            try {
                process(orderNo, now);
            } catch (Exception e) {
                log.warn("[pay-kernel-fun] query compensation failed, orderNo={}", orderNo, e);
                // 处理异常不代表订单状态已确定，重新安排一次检查，避免这笔订单从此失去补偿
                queryCompensationQueue.upsert(orderNo, nextCheckAt(now));
            }
        }
    }

    private void process(String orderNo, Date now) {
        Optional<TradeOrder> orderOpt = tradeOrderRepository.findByOrderNo(orderNo);
        if (!orderOpt.isPresent()) {
            return;
        }
        TradeOrder order = orderOpt.get();
        if (order.getStatus() != TradeStatus.PENDING) {
            return;
        }

        TradeQueryDetail detail = tradeQueryExecutorRegistry.require(order.getChannel()).queryDetail(orderNo);
        if (detail.isPaid()) {
            paymentConfirmer.confirmPaid(orderNo, detail.getPayTime() != null ? detail.getPayTime() : now);
            return;
        }

        if (order.getExpireTime() != null && !order.getExpireTime().after(now)) {
            log.info("[pay-kernel-fun] order expired, stop query compensation, orderNo={}", orderNo);
            return;
        }
        queryCompensationQueue.upsert(orderNo, nextCheckAt(now));
    }

    private Date nextCheckAt(Date now) {
        return new Date(now.getTime() + tradeProperties.getQueryRetryIntervalSeconds() * 1000);
    }
}
