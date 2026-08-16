package com.paykernelfun.launcher.biz.sample;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.paykernelfun.core.spi.biz.TradeOpenRejectedException;
import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsMapper;
import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsOrderMapper;
import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsOrderPO;
import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsOrderStatus;
import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 唯一操作 sample_goods / sample_goods_order 两张业务表的地方。
 *
 * 库存扣减采用下单预扣模式：{@link #reserveStock} 在下单时用 CAS UPDATE 原子扣库存并落一条 PENDING 业务订单；
 * 支付成功时只锁单（写 PAID + pay_time），不再碰库存；订单被关闭或退款时统一走 {@link #release} 把库存加回去。
 * 这与 {@code TradeOrderRepository} 的 CAS 模式（用 UPDATE ... WHERE status=? 的影响行数判断成功）是同一个思路。
 */
@Service
@RequiredArgsConstructor
public class SampleGoodsService {

    private final SampleGoodsMapper sampleGoodsMapper;
    private final SampleGoodsOrderMapper sampleGoodsOrderMapper;

    @Transactional
    public BigDecimal reserveStock(Long goodsId, Integer quantity, String buyerId, String orderNo, String bizOrderNo) {
        SampleGoodsPO goods = sampleGoodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new TradeOpenRejectedException("goods not found: " + goodsId);
        }

        LambdaUpdateWrapper<SampleGoodsPO> deductWrapper = new LambdaUpdateWrapper<SampleGoodsPO>()
                .eq(SampleGoodsPO::getId, goodsId)
                .ge(SampleGoodsPO::getStock, quantity)
                .setSql("stock = stock - " + quantity);
        int updated = sampleGoodsMapper.update(null, deductWrapper);
        if (updated == 0) {
            throw new TradeOpenRejectedException("库存不足: goodsId=" + goodsId);
        }

        BigDecimal amount = goods.getPrice().multiply(BigDecimal.valueOf(quantity));

        SampleGoodsOrderPO orderPO = new SampleGoodsOrderPO();
        orderPO.setOrderNo(orderNo);
        orderPO.setGoodsId(goodsId);
        orderPO.setGoodsName(goods.getGoodsName());
        orderPO.setUnitPrice(goods.getPrice());
        orderPO.setQuantity(quantity);
        orderPO.setAmount(amount);
        orderPO.setBuyerId(buyerId);
        orderPO.setStatus(SampleGoodsOrderStatus.PENDING.name());
        sampleGoodsOrderMapper.insert(orderPO);

        return amount;
    }

    @Transactional
    public void markPaid(String orderNo, Date payTime) {
        LambdaUpdateWrapper<SampleGoodsOrderPO> wrapper = new LambdaUpdateWrapper<SampleGoodsOrderPO>()
                .eq(SampleGoodsOrderPO::getOrderNo, orderNo)
                .eq(SampleGoodsOrderPO::getStatus, SampleGoodsOrderStatus.PENDING.name())
                .set(SampleGoodsOrderPO::getStatus, SampleGoodsOrderStatus.PAID.name())
                .set(SampleGoodsOrderPO::getPayTime, payTime);
        sampleGoodsOrderMapper.update(null, wrapper);
    }

    /** 关单和退款都要把预扣的库存还回去，区别只是落到哪个终态，所以共用这一个方法。 */
    @Transactional
    public void release(String orderNo, SampleGoodsOrderStatus targetStatus) {
        SampleGoodsOrderPO order = sampleGoodsOrderMapper.selectOne(
                new LambdaQueryWrapper<SampleGoodsOrderPO>().eq(SampleGoodsOrderPO::getOrderNo, orderNo));
        if (order == null) {
            return;
        }

        LambdaUpdateWrapper<SampleGoodsOrderPO> orderWrapper = new LambdaUpdateWrapper<SampleGoodsOrderPO>()
                .eq(SampleGoodsOrderPO::getOrderNo, orderNo)
                .set(SampleGoodsOrderPO::getStatus, targetStatus.name());
        int updated = sampleGoodsOrderMapper.update(null, orderWrapper);
        if (updated == 0) {
            return;
        }

        LambdaUpdateWrapper<SampleGoodsPO> stockWrapper = new LambdaUpdateWrapper<SampleGoodsPO>()
                .eq(SampleGoodsPO::getId, order.getGoodsId())
                .setSql("stock = stock + " + order.getQuantity());
        sampleGoodsMapper.update(null, stockWrapper);
    }

    @Transactional
    public Long createGoods(String goodsName, BigDecimal price, Integer stock) {
        SampleGoodsPO po = new SampleGoodsPO();
        po.setGoodsName(goodsName);
        po.setPrice(price);
        po.setStock(stock);
        sampleGoodsMapper.insert(po);
        return po.getId();
    }

    public List<SampleGoodsPO> listGoods() {
        return sampleGoodsMapper.selectList(null);
    }

    public SampleGoodsPO getGoods(Long goodsId) {
        return sampleGoodsMapper.selectById(goodsId);
    }

    public List<SampleGoodsOrderPO> listOrdersByGoods(Long goodsId) {
        return sampleGoodsOrderMapper.selectList(
                new LambdaQueryWrapper<SampleGoodsOrderPO>().eq(SampleGoodsOrderPO::getGoodsId, goodsId));
    }
}
