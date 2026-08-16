package com.paykernelfun.launcher.biz.sample;

import com.paykernelfun.core.spi.biz.TradeSuccessListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleGoodsSuccessListener implements TradeSuccessListener {

    private final SampleGoodsService sampleGoodsService;

    @Override
    public String bizType() {
        return SampleGoodsOpenHandler.BIZ_TYPE;
    }

    @Override
    public void onPaySuccess(String bizOrderNo, String orderNo) {
        sampleGoodsService.markPaid(orderNo, new Date());
        log.info("[sample] 锁单完成：bizOrderNo={}, orderNo={}", bizOrderNo, orderNo);
    }
}
