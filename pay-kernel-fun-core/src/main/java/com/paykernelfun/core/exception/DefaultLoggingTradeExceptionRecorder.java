package com.paykernelfun.core.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/** 兜底实现：只打日志，不落库。接入方需要持久化时应提供自己的 {@link TradeExceptionRecorder} Bean 覆盖它。 */
@Slf4j
public class DefaultLoggingTradeExceptionRecorder implements TradeExceptionRecorder {

    @Override
    public void record(Consumer<TradeExceptionRecord.Builder> customizer) {
        TradeExceptionRecord.Builder builder = new TradeExceptionRecord.Builder();
        customizer.accept(builder);
        TradeExceptionRecord record = builder.build();
        log.warn("[pay-kernel-fun] trade exception: channel={}, orderNo={}, scene={}, message={}",
                record.getChannel(), record.getOrderNo(), record.getScene(), record.getMessage());
    }
}
