package com.paykernelfun.core.spi.biz;

/**
 * 业务方在 {@link TradeOpenHandler#buildOrder} 里判断这笔下单不该继续时抛出（例如库存不足）。
 * infra 层统一捕获处理成 4xx 响应，不需要认识具体业务的拒绝原因。
 */
public class TradeOpenRejectedException extends RuntimeException {
    public TradeOpenRejectedException(String message) {
        super(message);
    }
}
