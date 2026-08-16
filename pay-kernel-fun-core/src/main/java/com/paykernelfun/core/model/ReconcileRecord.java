package com.paykernelfun.core.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/** 对账拉取到的一笔渠道侧交易记录，由 {@link com.paykernelfun.core.spi.channel.TradeReconcileHandler#fetch} 返回。 */
@Data
public class ReconcileRecord {
    /** 内部交易流水号，对应 TradeOrder.orderNo */
    private String orderNo;
    /** 渠道侧交易号 */
    private String channelTradeNo;
    /** 渠道侧记录的交易金额 */
    private BigDecimal amount;
    /** 渠道侧记录的交易时间 */
    private Date tradeTime;
    /** 渠道侧的原始交易状态描述，不同渠道格式不同，不做统一映射 */
    private String channelStatus;
}
