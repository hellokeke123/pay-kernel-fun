package com.paykernelfun.launcher.biz.sample;

import com.paykernelfun.core.model.TradeOpenRequest;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.spi.biz.TradeOpenHandler;
import com.paykernelfun.core.spi.biz.TradeOpenRejectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 示例业务：一个有真实库存的商品下单场景。只实现 core 的业务回调 SPI，不接触 HTTP 层、不知道支付宝怎么下单——
 * 这是本项目要证明的核心：新增业务不需要碰渠道代码，也不需要新增 Controller。
 *
 * 下单请求体的 extra 需要带 goodsId/quantity/buyerId（见 {@code CreateTradeRequest.extra}），
 * 金额由 {@link SampleGoodsService#reserveStock} 按商品当前单价计算，不信任客户端传入的金额。
 */
@Component
@RequiredArgsConstructor
public class SampleGoodsOpenHandler implements TradeOpenHandler {

    public static final String BIZ_TYPE = "SAMPLE_GOODS";

    private final SampleGoodsService sampleGoodsService;

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public TradeOrder buildOrder(TradeOpenRequest request) {
        Long goodsId = readLong(request, "goodsId");
        Integer quantity = readInt(request, "quantity");
        String buyerId = readString(request, "buyerId");

        String orderNo = "PKF" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
        BigDecimal amount = sampleGoodsService.reserveStock(goodsId, quantity, buyerId, orderNo, request.getBizOrderNo());

        TradeOrder order = new TradeOrder();
        order.setOrderNo(orderNo);
        order.setBizOrderNo(request.getBizOrderNo());
        order.setBizType(BIZ_TYPE);
        order.setChannel(request.getChannel());
        order.setAmount(amount);
        return order;
    }

    private Long readLong(TradeOpenRequest request, String key) {
        Object value = request.getExtra().get(key);
        if (value == null) {
            throw new TradeOpenRejectedException("missing extra." + key);
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Integer readInt(TradeOpenRequest request, String key) {
        Object value = request.getExtra().get(key);
        if (value == null) {
            throw new TradeOpenRejectedException("missing extra." + key);
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private String readString(TradeOpenRequest request, String key) {
        Object value = request.getExtra().get(key);
        if (value == null) {
            throw new TradeOpenRejectedException("missing extra." + key);
        }
        return String.valueOf(value);
    }
}
