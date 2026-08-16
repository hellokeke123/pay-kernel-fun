package com.paykernelfun.launcher.infra.task;

import com.paykernelfun.core.compensation.TradeCloseCompensationQueue;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.model.TradeStatus;
import com.paykernelfun.core.repository.TradeOrderRepository;
import com.paykernelfun.launcher.infra.trade.TradeOrderCloser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * 到期自动关单任务：订单到达有效期截止时间仍未支付，主动关单，避免长期占用渠道侧的交易资源。
 *
 * 下单时把订单按有效期截止时间写入 {@link TradeCloseCompensationQueue}（见 {@code TradeController#createOrder}）；
 * 每次触发一直从队列里取一条到期订单处理，直到取不到为止（不做批量），若订单仍是 PENDING 就委托
 * {@link TradeOrderCloser} 关单（调渠道 + 清理补偿队列 + 路由 {@code TradeCloseListener} 业务回调）——
 * 与手动关单接口共用同一份收尾逻辑，不在这里重复写。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradeCloseCompensationTask {

    private final TradeCloseCompensationQueue closeCompensationQueue;
    private final TradeOrderRepository tradeOrderRepository;
    private final TradeOrderCloser tradeOrderCloser;

    @Scheduled(fixedDelayString = "${pay-kernel-fun.trade.poll-interval-millis:5000}")
    public void run() {
        Date now = new Date();
        Optional<String> next;
        while ((next = closeCompensationQueue.pollOneDue(now)).isPresent()) {
            String orderNo = next.get();
            try {
                process(orderNo);
            } catch (Exception e) {
                log.warn("[pay-kernel-fun] close compensation failed, orderNo={}", orderNo, e);
                // 关单失败不代表订单已经妥善处理，重新排回队列，下一轮再试
                closeCompensationQueue.upsert(orderNo, now);
            }
        }
    }

    private void process(String orderNo) {
        Optional<TradeOrder> orderOpt = tradeOrderRepository.findByOrderNo(orderNo);
        if (!orderOpt.isPresent()) {
            return;
        }
        TradeOrder order = orderOpt.get();
        if (order.getStatus() != TradeStatus.PENDING) {
            // 已经支付成功/已退款/已关闭，说明其它路径先处理了，两个补偿队列的清理由那条路径负责
            return;
        }
        tradeOrderCloser.close(order);
    }
}
