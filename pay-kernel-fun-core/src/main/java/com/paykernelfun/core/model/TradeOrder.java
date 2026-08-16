package com.paykernelfun.core.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 渠道无关的一次性交易订单模型。持久化方式由接入方通过 {@link com.paykernelfun.core.repository.TradeOrderRepository} 实现，
 * core 模块本身不绑定任何数据库/ORM。
 */
@Data
public class TradeOrder {

    /** 主键，由持久化实现回填 */
    private Long id;
    /** 内部交易流水号，全局唯一，渠道下单/回调/查询/退款均以此为主键 */
    private String orderNo;
    /** 业务方自己的订单号 */
    private String bizOrderNo;
    /** 业务类型，由接入方自行定义，用于路由 {@link com.paykernelfun.core.spi.biz.TradeSuccessListener} 等 */
    private String bizType;
    /** 支付渠道标识，如 ALIPAY，用于路由渠道侧 SPI */
    private String channel;
    /** 订单金额 */
    private BigDecimal amount;
    /** 币种，暂只支持人民币 */
    private String currency = "CNY";
    /** 订单状态，见 {@link TradeStatus} */
    private TradeStatus status = TradeStatus.PENDING;
    /** 渠道侧交易号 */
    private String channelTradeNo;
    /** 支付成功时间 */
    private Date payTime;
    /** 退款成功时间 */
    private Date refundTime;
    /** 订单有效期截止时间，用于补偿查询任务判断何时放弃、以及到期自动关单任务的触发时机 */
    private Date expireTime;
    /** 创建时间 */
    private Date createTime;
    /** 最后更新时间 */
    private Date updateTime;
    /** 渠道自定义扩展字段 */
    private Map<String, Object> extra = new HashMap<>();
}
