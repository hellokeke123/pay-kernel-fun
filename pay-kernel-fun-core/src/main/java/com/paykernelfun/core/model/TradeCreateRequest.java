package com.paykernelfun.core.model;

import lombok.Data;

/**
 * 向渠道发起下单所需的参数，由 {@link TradeOrder} 加上渠道回跳/异步通知地址组成。
 */
@Data
public class TradeCreateRequest {
    /** 待下单的订单（已落库，orderNo/amount 等已确定） */
    private TradeOrder order;
    /** 下单时展示给用户的商品/订单标题 */
    private String subject;
    /** 用户完成支付后浏览器同步跳转回的地址 */
    private String returnUrl;
    /** 渠道异步通知地址 */
    private String notifyUrl;
}
