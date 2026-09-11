package com.paykernelfun.subscription.model;

import lombok.Data;

import java.util.Date;

/**
 * 订阅场景的领域模型，与一次性交易的 {@code TradeOrder}（pay-kernel-fun-core）刻意分开——
 * 订阅有续期周期、自动续订、恢复购买等一次性支付没有的语义，混在一个模型里会互相污染。
 */
@Data
public class Subscription {
    /** 主键，由持久化实现回填 */
    private Long id;
    /** 订阅号，全局唯一 */
    private String subscriptionNo;
    /** 业务方用户标识，具体含义由接入方定义 */
    private String bizUserId;
    /** 支付渠道标识，如 APPLE/GOOGLE/ALIPAY */
    private String channel;
    /** 渠道侧的产品/订阅计划标识 */
    private String productId;
    /** 订阅状态，见 {@link SubscriptionStatus} */
    private SubscriptionStatus status = SubscriptionStatus.PENDING;
    /** 当前订阅周期起始时间 */
    private Date periodStart;
    /** 当前订阅周期截止时间 */
    private Date periodEnd;
    /** 是否开启自动续订 */
    private boolean autoRenew;
    /** 创建时间 */
    private Date createTime;
    /** 最后更新时间 */
    private Date updateTime;
}
