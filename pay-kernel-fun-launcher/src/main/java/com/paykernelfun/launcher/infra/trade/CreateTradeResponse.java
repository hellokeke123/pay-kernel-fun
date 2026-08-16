package com.paykernelfun.launcher.infra.trade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTradeResponse {
    /** 内部交易流水号 */
    private String orderNo;
    /** 渠道下单结果的载荷：支付宝 PC 网站支付是自动提交表单 HTML，App/小程序场景可能是别的格式 */
    private String payload;
}
