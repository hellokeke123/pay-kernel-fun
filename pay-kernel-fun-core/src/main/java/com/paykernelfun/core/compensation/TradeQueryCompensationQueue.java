package com.paykernelfun.core.compensation;

import java.util.Date;
import java.util.Optional;

/**
 * 支付结果补偿查询队列：记录"下一次该检查这笔订单支付状态的时间"，供定时任务轮询，
 * 用于弥补渠道异步回调丢失/延迟的场景。
 *
 * core 不假设具体存储介质（Redis ZSet、数据库均可实现），只声明"延迟到期后能取出"的语义：
 * 下单时用 {@link #upsert} 安排首次检查；每次检查未支付成功时再次 {@link #upsert} 顺延；
 * 一旦查询到支付成功或订单已过期放弃补偿，调用方负责 {@link #remove}。
 *
 * 一次只取一条（{@link #pollOneDue}），不做批量：调用方负责在一个循环里反复取、逐条处理，
 * 直到取不到为止，避免一次取一批导致某一条处理慢时其它到期订单被卡住。
 */
public interface TradeQueryCompensationQueue {

    /** 安排或覆盖某笔订单的下一次检查时间 */
    void upsert(String orderNo, Date nextCheckAt);

    void remove(String orderNo);

    /** 原子地取出并移除一个到期（nextCheckAt &lt;= now）的订单号；没有到期成员则返回 empty */
    Optional<String> pollOneDue(Date now);
}
