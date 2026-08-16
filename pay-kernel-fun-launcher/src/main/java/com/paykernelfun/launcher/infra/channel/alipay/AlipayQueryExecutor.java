package com.paykernelfun.launcher.infra.channel.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.paykernelfun.core.model.TradeQueryDetail;
import com.paykernelfun.core.spi.channel.TradeChannelException;
import com.paykernelfun.core.spi.channel.TradeQueryExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AlipayQueryExecutor implements TradeQueryExecutor {

    private final AlipayClient alipayClient;

    @Override
    public String channel() {
        return AlipayTradeCreateHandler.CHANNEL;
    }

    @Override
    public boolean isPaid(String orderNo) {
        return queryDetail(orderNo).isPaid();
    }

    @Override
    public TradeQueryDetail queryDetail(String orderNo) {
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderNo);

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);

        TradeQueryDetail detail = new TradeQueryDetail();
        detail.setOrderNo(orderNo);
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                boolean paid = "TRADE_SUCCESS".equals(response.getTradeStatus())
                        || "TRADE_FINISHED".equals(response.getTradeStatus());
                detail.setPaid(paid);
                detail.setChannelTradeNo(response.getTradeNo());
                if (paid && response.getTotalAmount() != null) {
                    detail.setPaidAmount(new BigDecimal(response.getTotalAmount()));
                }
            }
        } catch (AlipayApiException e) {
            throw new TradeChannelException("支付宝查询调用异常: " + e.getMessage(), e);
        }
        return detail;
    }
}
