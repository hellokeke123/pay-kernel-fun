package com.paykernelfun.launcher.infra.channel.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.paykernelfun.core.lock.TradeLock;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.registry.ChannelRegistry;
import com.paykernelfun.core.repository.TradeOrderRepository;
import com.paykernelfun.core.spi.biz.TradeRefundListener;
import com.paykernelfun.core.spi.channel.TradeChannelException;
import com.paykernelfun.core.spi.channel.TradeRefundExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付宝退款是同步的：受理成功即代表退款完成，可以直接在这里收尾（与 Payssion/XPay 等异步退款渠道不同，
 * 那类渠道只应发起退款、返回渠道退款单号，最终状态要等退款回调）。
 */
@Component
@RequiredArgsConstructor
public class AlipayRefundExecutor implements TradeRefundExecutor {

    private static final String LOCK_KEY_PREFIX = "pay-kernel-fun:lock:trade:";
    private static final long LOCK_LEASE_MILLIS = 5000L;

    private final AlipayClient alipayClient;
    private final TradeOrderRepository tradeOrderRepository;
    private final TradeLock tradeLock;
    private final ChannelRegistry<TradeRefundListener> tradeRefundListenerRegistry;

    @Override
    public String channel() {
        return AlipayTradeCreateHandler.CHANNEL;
    }

    @Override
    public String refund(String orderNo, BigDecimal amount, String reason) {
        return tradeLock.executeIfLocked(LOCK_KEY_PREFIX + orderNo, LOCK_LEASE_MILLIS, () -> {
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(orderNo);
            model.setRefundAmount(amount.toPlainString());
            model.setRefundReason(reason);

            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizModel(model);

            try {
                AlipayTradeRefundResponse response = alipayClient.execute(request);
                if (!response.isSuccess()) {
                    throw new TradeChannelException("支付宝退款失败: " + response.getSubMsg());
                }
                TradeOrder order = tradeOrderRepository.findByOrderNo(orderNo)
                        .orElseThrow(() -> new TradeChannelException("order not found: " + orderNo));
                boolean marked = tradeOrderRepository.casMarkRefunded(orderNo, new Date());
                if (marked) {
                    tradeRefundListenerRegistry.find(order.getBizType())
                            .ifPresent(listener -> listener.onRefundSuccess(orderNo));
                }
                return response.getTradeNo();
            } catch (AlipayApiException e) {
                throw new TradeChannelException("支付宝退款调用异常: " + e.getMessage(), e);
            }
        }).orElseThrow(() -> new TradeChannelException("退款正在处理中，请稍后重试: " + orderNo));
    }
}
