package com.paykernelfun.launcher.biz.sample;

import com.paykernelfun.core.spi.biz.TradeCloseListener;
import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单超时未支付被关单时的业务回调：把下单时预扣的库存加回去。
 * 没有这个监听器，预扣的库存在订单被关单后就永久卡死，是"下单预扣库存"模式必须配套的一环。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SampleGoodsCloseListener implements TradeCloseListener {

    private final SampleGoodsService sampleGoodsService;

    @Override
    public String bizType() {
        return SampleGoodsOpenHandler.BIZ_TYPE;
    }

    @Override
    public void onOrderClosed(String orderNo) {
        sampleGoodsService.release(orderNo, SampleGoodsOrderStatus.CLOSED);
        log.info("[sample] 订单已关闭，库存已回滚：orderNo={}", orderNo);
    }
}
