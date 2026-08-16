package com.paykernelfun.launcher.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 交易生命周期相关配置：订单有效期、补偿查询轮询间隔，均走 yaml 不落库。 */
@Data
@Component
@ConfigurationProperties(prefix = "pay-kernel-fun.trade")
public class TradeProperties {
    /** 订单从创建到自动关单的有效期 */
    private long orderValiditySeconds = 900;
    /** 补偿查询任务：一次查询未支付成功后，下一次重试的间隔 */
    private long queryRetryIntervalSeconds = 10;
    /** 两个补偿任务的轮询间隔（毫秒），即 @Scheduled 的 fixedDelay */
    private long pollIntervalMillis = 5000;
    /** 用户支付完成后浏览器跳转回的地址，同步跳转不代表支付一定成功，最终以异步通知/查单为准；渠道无关，由 TradeController 统一传给各渠道下单 */
    private String returnUrl;
}
