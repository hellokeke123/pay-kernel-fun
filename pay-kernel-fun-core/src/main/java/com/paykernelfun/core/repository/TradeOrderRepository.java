package com.paykernelfun.core.repository;

import com.paykernelfun.core.model.TradeOrder;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 订单持久化 SPI。core 不依赖任何数据库/ORM，接入方自行实现（MyBatis-Plus、JPA、内存皆可）。
 * cas* 系列方法要求实现方做条件更新（例如 {@code WHERE status = 'PENDING'}），并发的重复回调/查单只应有一次成功。
 */
public interface TradeOrderRepository {

    void save(TradeOrder order);

    Optional<TradeOrder> findByOrderNo(String orderNo);

    boolean casMarkPaid(String orderNo, Date payTime);

    boolean casMarkClosed(String orderNo);

    boolean casMarkRefunded(String orderNo, Date refundTime);

    List<TradeOrder> findCreatedOn(LocalDate date);
}
