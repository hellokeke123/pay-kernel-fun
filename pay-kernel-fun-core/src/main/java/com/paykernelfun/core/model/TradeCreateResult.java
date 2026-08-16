package com.paykernelfun.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 渠道下单结果。redirectUrl 用于网页跳转类支付宝下单场景；payload 用于 App/小程序等需要客户端自行拉起支付的场景。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeCreateResult {
    /** 内部交易流水号，原样透传自下单请求 */
    private String orderNo;
    /** 网页跳转类下单场景使用：渠道收银台的跳转地址 */
    private String redirectUrl;
    /** App/小程序/网页表单类下单场景使用：需要客户端自行处理的载荷（如支付宝 PC 网站支付返回的自动提交表单 HTML） */
    private String payload;
}
