package com.paykernelfun.launcher.infra.channel.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.paykernelfun.core.model.TradeCreateRequest;
import com.paykernelfun.core.model.TradeCreateResult;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.spi.channel.TradeChannelException;
import com.paykernelfun.core.spi.channel.TradeCreateHandler;
import com.paykernelfun.launcher.infra.config.AlipayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlipayTradeCreateHandler implements TradeCreateHandler {

    public static final String CHANNEL = "ALIPAY";

    private final AlipayClient alipayClient;
    private final AlipayProperties properties;

    @Override
    public String channel() {
        return CHANNEL;
    }

    @Override
    public TradeCreateResult create(TradeCreateRequest request) {
        TradeOrder order = request.getOrder();

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(order.getOrderNo());
        model.setTotalAmount(order.getAmount().toPlainString());
        model.setSubject(request.getSubject());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setBizModel(model);
        payRequest.setNotifyUrl(properties.getNotifyUrl());
        payRequest.setReturnUrl(request.getReturnUrl());

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(payRequest);
            if (!response.isSuccess()) {
                throw new TradeChannelException("支付宝下单失败: " + response.getSubMsg());
            }
            return new TradeCreateResult(order.getOrderNo(), null, response.getBody());
        } catch (AlipayApiException e) {
            throw new TradeChannelException("支付宝下单调用异常: " + e.getMessage(), e);
        }
    }
}
