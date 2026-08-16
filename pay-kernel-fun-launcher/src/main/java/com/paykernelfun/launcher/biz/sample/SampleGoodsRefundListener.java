package com.paykernelfun.launcher.biz.sample;

import com.paykernelfun.core.spi.biz.TradeRefundListener;
import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleGoodsRefundListener implements TradeRefundListener {

    private final SampleGoodsService sampleGoodsService;

    @Override
    public String bizType() {
        return SampleGoodsOpenHandler.BIZ_TYPE;
    }

    @Override
    public void onRefundSuccess(String orderNo) {
        sampleGoodsService.release(orderNo, SampleGoodsOrderStatus.REFUNDED);
        log.info("[sample] 退款完成，库存已回滚：orderNo={}", orderNo);
    }

    @Override
    public void onRefundRevoked(String orderNo) {
        log.info("[sample] 退款被撤销：orderNo={}", orderNo);
    }
}
