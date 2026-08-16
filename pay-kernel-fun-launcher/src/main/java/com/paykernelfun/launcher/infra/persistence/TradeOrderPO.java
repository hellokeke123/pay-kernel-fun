package com.paykernelfun.launcher.infra.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("trade_order")
public class TradeOrderPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 内部交易流水号，全局唯一 */
    private String orderNo;
    /** 业务方自己的订单号 */
    private String bizOrderNo;
    /** 业务类型，用于路由业务回调 SPI */
    private String bizType;
    /** 支付渠道标识，如 ALIPAY */
    private String channel;
    /** 订单金额 */
    private BigDecimal amount;
    /** 币种 */
    private String currency;
    /** 订单状态：PENDING/SUCCESS/CLOSED/REFUNDED，对应 TradeStatus 枚举名 */
    private String status;
    /** 渠道侧交易号 */
    private String channelTradeNo;
    /** TradeOrder.extra 的 JSON 序列化结果 */
    private String extra;
    /** 支付成功时间 */
    private Date payTime;
    /** 退款成功时间 */
    private Date refundTime;
    /** 订单有效期截止时间 */
    private Date expireTime;
    /** 创建时间 */
    private Date createTime;
    /** 最后更新时间 */
    private Date updateTime;
}
