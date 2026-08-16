package com.paykernelfun.core.spi.channel;

import com.paykernelfun.core.model.ReconcileRecord;

import java.time.LocalDate;
import java.util.List;

/** 拉取渠道侧某天的交易记录用于对账。案例应用不实现该接口，仅作为抽象层的扩展点保留。 */
public interface TradeReconcileHandler {
    String channel();

    List<ReconcileRecord> fetch(LocalDate date);
}
