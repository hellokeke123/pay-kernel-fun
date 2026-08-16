package com.paykernelfun.core.spi.channel;

/**
 * 渠道 SPI 实现调用外部渠道接口时，渠道明确返回业务性失败（如"交易不存在"）
 * 或调用异常时抛出，用于和"我方代码 bug"区分开。infra 层统一捕获处理成 4xx/5xx 响应并把
 * 渠道原始错误信息带给调用方，不需要吞掉异常也不需要让调用方看到裸的 500。
 */
public class TradeChannelException extends RuntimeException {
    public TradeChannelException(String message) {
        super(message);
    }

    public TradeChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
