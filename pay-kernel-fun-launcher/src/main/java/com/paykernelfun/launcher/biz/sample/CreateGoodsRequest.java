package com.paykernelfun.launcher.biz.sample;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateGoodsRequest {
    /** 商品名 */
    private String goodsName;
    /** 单价 */
    private BigDecimal price;
    /** 初始库存 */
    private Integer stock;
}
