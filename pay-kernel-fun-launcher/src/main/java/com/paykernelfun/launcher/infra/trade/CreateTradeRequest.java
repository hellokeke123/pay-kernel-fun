package com.paykernelfun.launcher.infra.trade;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** 通用下单请求：由业务方指定 bizType 路由到自己的 {@code TradeOpenHandler}，与具体业务无关。 */
@Data
public class CreateTradeRequest {
    /** 业务方自己的订单号 */
    private String bizOrderNo;
    /** 业务类型，路由到对应的 TradeOpenHandler */
    private String bizType;
    /** 指定走哪个支付渠道，如 ALIPAY */
    private String channel;
    /** 金额；具体业务如需按服务端数据现算金额，可以不传 */
    private BigDecimal amount;
    /** 业务方自定义参数，具体字段由各业务自行约定 */
    private Map<String, Object> extra = new HashMap<>();
}
