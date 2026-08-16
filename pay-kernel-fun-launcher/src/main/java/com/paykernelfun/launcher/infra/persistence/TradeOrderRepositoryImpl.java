package com.paykernelfun.launcher.infra.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.model.TradeStatus;
import com.paykernelfun.core.repository.TradeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TradeOrderRepositoryImpl implements TradeOrderRepository {

    private final TradeOrderMapper tradeOrderMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void save(TradeOrder order) {
        TradeOrderPO po = toPO(order);
        tradeOrderMapper.insert(po);
        order.setId(po.getId());
    }

    @Override
    public Optional<TradeOrder> findByOrderNo(String orderNo) {
        TradeOrderPO po = tradeOrderMapper.selectOne(
                new LambdaQueryWrapper<TradeOrderPO>().eq(TradeOrderPO::getOrderNo, orderNo));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public boolean casMarkPaid(String orderNo, Date payTime) {
        LambdaUpdateWrapper<TradeOrderPO> wrapper = new LambdaUpdateWrapper<TradeOrderPO>()
                .eq(TradeOrderPO::getOrderNo, orderNo)
                .eq(TradeOrderPO::getStatus, TradeStatus.PENDING.name())
                .set(TradeOrderPO::getStatus, TradeStatus.SUCCESS.name())
                .set(TradeOrderPO::getPayTime, payTime);
        return tradeOrderMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean casMarkClosed(String orderNo) {
        LambdaUpdateWrapper<TradeOrderPO> wrapper = new LambdaUpdateWrapper<TradeOrderPO>()
                .eq(TradeOrderPO::getOrderNo, orderNo)
                .eq(TradeOrderPO::getStatus, TradeStatus.PENDING.name())
                .set(TradeOrderPO::getStatus, TradeStatus.CLOSED.name());
        return tradeOrderMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean casMarkRefunded(String orderNo, Date refundTime) {
        LambdaUpdateWrapper<TradeOrderPO> wrapper = new LambdaUpdateWrapper<TradeOrderPO>()
                .eq(TradeOrderPO::getOrderNo, orderNo)
                .eq(TradeOrderPO::getStatus, TradeStatus.SUCCESS.name())
                .set(TradeOrderPO::getStatus, TradeStatus.REFUNDED.name())
                .set(TradeOrderPO::getRefundTime, refundTime);
        return tradeOrderMapper.update(null, wrapper) > 0;
    }

    @Override
    public List<TradeOrder> findCreatedOn(LocalDate date) {
        Date start = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<TradeOrderPO> list = tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrderPO>()
                .ge(TradeOrderPO::getCreateTime, start)
                .lt(TradeOrderPO::getCreateTime, end));
        return list.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private TradeOrderPO toPO(TradeOrder order) {
        TradeOrderPO po = new TradeOrderPO();
        po.setId(order.getId());
        po.setOrderNo(order.getOrderNo());
        po.setBizOrderNo(order.getBizOrderNo());
        po.setBizType(order.getBizType());
        po.setChannel(order.getChannel());
        po.setAmount(order.getAmount());
        po.setCurrency(order.getCurrency());
        po.setStatus(order.getStatus().name());
        po.setChannelTradeNo(order.getChannelTradeNo());
        po.setPayTime(order.getPayTime());
        po.setRefundTime(order.getRefundTime());
        po.setExpireTime(order.getExpireTime());
        po.setExtra(writeExtra(order.getExtra()));
        return po;
    }

    private TradeOrder toDomain(TradeOrderPO po) {
        TradeOrder order = new TradeOrder();
        order.setId(po.getId());
        order.setOrderNo(po.getOrderNo());
        order.setBizOrderNo(po.getBizOrderNo());
        order.setBizType(po.getBizType());
        order.setChannel(po.getChannel());
        order.setAmount(po.getAmount());
        order.setCurrency(po.getCurrency());
        order.setStatus(TradeStatus.valueOf(po.getStatus()));
        order.setChannelTradeNo(po.getChannelTradeNo());
        order.setPayTime(po.getPayTime());
        order.setRefundTime(po.getRefundTime());
        order.setExpireTime(po.getExpireTime());
        order.setCreateTime(po.getCreateTime());
        order.setUpdateTime(po.getUpdateTime());
        order.setExtra(readExtra(po.getExtra()));
        return order;
    }

    private String writeExtra(Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(extra);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("serialize TradeOrder.extra failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readExtra(String extra) {
        if (extra == null || extra.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(extra, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("deserialize TradeOrder.extra failed", e);
        }
    }
}
