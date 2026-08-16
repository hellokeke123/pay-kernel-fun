package com.paykernelfun.core.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务方发起下单的统一入参，交给对应 bizType 的 {@link com.paykernelfun.core.spi.biz.TradeOpenHandler} 构造 {@link TradeOrder}。
 */
@Data
public class TradeOpenRequest {
    /** 业务方自己的订单号 */
    private String bizOrderNo;
    /** 业务类型，用于路由到对应的 TradeOpenHandler */
    private String bizType;
    /** 指定走哪个支付渠道，如 ALIPAY */
    private String channel;
    /** 业务方指定的金额；具体业务如需按服务端数据现算金额（如按商品单价计算），可以忽略这个字段自行计算 */
    private BigDecimal amount;
    /** 业务方自定义参数，具体字段由各业务自行约定（如商品ID、数量、买家标识） */
    private Map<String, Object> extra = new HashMap<>();
}
