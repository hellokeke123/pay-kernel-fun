package com.paykernelfun.launcher.infra.channel.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.repository.TradeOrderRepository;
import com.paykernelfun.core.spi.channel.TradeChannelException;
import com.paykernelfun.core.spi.channel.TradeCloseExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 关单场景需要兼容一种正常情况：PC 网站支付（alipay.trade.page.pay）下单接口只是生成跳转表单，
 * 真正在支付宝侧建立交易记录的时机是用户浏览器提交表单打开收银台——如果用户一直没打开支付页面就
 * 到了本地超时时间，支付宝侧根本没有这笔交易，关单会收到 {@code ACQ.TRADE_NOT_EXIST}。
 * 这种情况从本地视角看等同于"已经不需要关"，直接按关单成功处理，不当作失败重试。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlipayCloseExecutor implements TradeCloseExecutor {

    private static final String SUB_CODE_TRADE_NOT_EXIST = "ACQ.TRADE_NOT_EXIST";

    private final AlipayClient alipayClient;
    private final TradeOrderRepository tradeOrderRepository;

    @Override
    public String channel() {
        return AlipayTradeCreateHandler.CHANNEL;
    }

    @Override
    public boolean close(TradeOrder order) {
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(order.getOrderNo());

        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);

        try {
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            if (!response.isSuccess() && !SUB_CODE_TRADE_NOT_EXIST.equals(response.getSubCode())) {
                return false;
            }
            if (SUB_CODE_TRADE_NOT_EXIST.equals(response.getSubCode())) {
                log.info("[pay-kernel-fun] alipay trade not exist, treat as already closed, orderNo={}",
                        order.getOrderNo());
            }
            return tradeOrderRepository.casMarkClosed(order.getOrderNo());
        } catch (AlipayApiException e) {
            throw new TradeChannelException("支付宝关单调用异常: " + e.getMessage(), e);
        }
    }
}

