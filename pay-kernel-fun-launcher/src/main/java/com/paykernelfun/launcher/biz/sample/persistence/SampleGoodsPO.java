package com.paykernelfun.launcher.biz.sample.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sample_goods")
public class SampleGoodsPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 商品名 */
    private String goodsName;
    /** 单价 */
    private BigDecimal price;
    /** 可售库存，下单预扣、关单/退款回滚 */
    private Integer stock;
    /** 创建时间 */
    private Date createTime;
    /** 最后更新时间 */
    private Date updateTime;
}
