package com.paykernelfun.launcher.infra.channel.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.paykernelfun.core.exception.TradeExceptionRecorder;
import com.paykernelfun.core.model.RawNotification;
import com.paykernelfun.core.spi.channel.TradeNotifyHandler;
import com.paykernelfun.launcher.infra.config.AlipayProperties;
import com.paykernelfun.launcher.infra.trade.TradePaymentConfirmer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 支付宝异步回调处理：验签 -> 判断交易状态 -> 委托 {@link TradePaymentConfirmer} 完成"确认支付成功"的收尾。
 * 收尾逻辑（加锁/CAS/路由业务回调/清理补偿队列）与 {@code TradeQueryCompensationTask} 共用同一份实现，
 * 不在这里重复写一遍，避免两条触发路径的处理逻辑走样。
 */
@Component
@RequiredArgsConstructor
public class AlipayNotifyHandler implements TradeNotifyHandler {

    private final AlipayProperties properties;
    private final TradePaymentConfirmer paymentConfirmer;
    private final TradeExceptionRecorder exceptionRecorder;

    @Override
    public String channel() {
        return AlipayTradeCreateHandler.CHANNEL;
    }

    @Override
    public void handleNotify(RawNotification notification) {
        Map<String, String> params = notification.getParams();
        String orderNo = params.get("out_trade_no");

        boolean signValid;
        try {
            signValid = AlipaySignature.rsaCheckV1(
                    params, properties.getAlipayPublicKey(), properties.getCharset(), properties.getSignType());
        } catch (AlipayApiException e) {
            throw new IllegalStateException("Alipay notify sign check error", e);
        }
        if (!signValid) {
            exceptionRecorder.record(b -> b.channel(channel()).orderNo(orderNo).scene("notify")
                    .message("sign check failed"));
            return;
        }

        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return;
        }

        paymentConfirmer.confirmPaid(orderNo, new Date());
    }
}
