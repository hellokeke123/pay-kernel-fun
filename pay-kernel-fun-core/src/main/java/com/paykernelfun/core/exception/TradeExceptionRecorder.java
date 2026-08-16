package com.paykernelfun.core.exception;

import java.util.function.Consumer;

/**
 * 记录支付链路上需要人工介入排查的异常。实现方不应让记录失败影响调用方主流程
 * （不抛异常、不回滚外层事务），core 只提供接口，默认实现见 {@link DefaultLoggingTradeExceptionRecorder}。
 */
public interface TradeExceptionRecorder {
    void record(Consumer<TradeExceptionRecord.Builder> customizer);
}
