package com.paykernelfun.launcher.biz.sample;

import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsOrderPO;
import com.paykernelfun.launcher.biz.sample.persistence.SampleGoodsPO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 示例业务自己的管理接口，只负责商品的增/查和"某个商品下有哪些订单"，
 * 不重复实现任何交易端点（下单/回调/退款/关单一律走 {@code TradeController}）。
 */
@RestController
@RequiredArgsConstructor
public class SampleGoodsController {

    private final SampleGoodsService sampleGoodsService;

    @PostMapping("/sample/goods")
    public Map<String, Long> createGoods(@RequestBody CreateGoodsRequest req) {
        Long id = sampleGoodsService.createGoods(req.getGoodsName(), req.getPrice(), req.getStock());
        return Collections.singletonMap("id", id);
    }

    @GetMapping("/sample/goods")
    public List<SampleGoodsPO> listGoods() {
        return sampleGoodsService.listGoods();
    }

    @GetMapping("/sample/goods/{id}")
    public SampleGoodsPO getGoods(@PathVariable Long id) {
        return sampleGoodsService.getGoods(id);
    }

    @GetMapping("/sample/goods/{id}/orders")
    public List<SampleGoodsOrderPO> listOrders(@PathVariable Long id) {
        return sampleGoodsService.listOrdersByGoods(id);
    }
}
