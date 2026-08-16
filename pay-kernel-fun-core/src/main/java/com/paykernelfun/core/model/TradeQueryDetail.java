package com.paykernelfun.core.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/** 主动查询渠道支付状态的结果，由 {@link com.paykernelfun.core.spi.channel.TradeQueryExecutor#queryDetail} 返回。 */
@Data
public class TradeQueryDetail {
    /** 内部交易流水号 */
    private String orderNo;
    /** 渠道侧交易号 */
    private String channelTradeNo;
    /** 渠道侧是否已支付成功 */
    private boolean paid;
    /** 渠道侧记录的实付金额 */
    private BigDecimal paidAmount;
    /** 渠道侧记录的支付完成时间 */
    private Date payTime;
}
