package com.paykernelfun.core.compensation;

import java.util.Date;
import java.util.Optional;

/**
 * 到期自动关单队列：记录订单的有效期截止时间，到期后由定时任务检查是否仍未支付、未支付则触发关单。
 * 语义与 {@link TradeQueryCompensationQueue} 平行但用途不同（一个是"还没查到结果就继续查"，
 * 一个是"到了截止时间就不再等，尝试关单"），不合并成一个队列以免触发条件互相干扰。
 *
 * 与 {@link TradeQueryCompensationQueue} 一样一次只取一条，调用方循环取空为止。
 */
public interface TradeCloseCompensationQueue {

    void upsert(String orderNo, Date expireTime);

    void remove(String orderNo);

    Optional<String> pollOneDue(Date now);
}
