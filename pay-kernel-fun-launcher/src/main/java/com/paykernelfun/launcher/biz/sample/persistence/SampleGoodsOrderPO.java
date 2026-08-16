package com.paykernelfun.launcher.biz.sample.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sample_goods_order")
public class SampleGoodsOrderPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 对应 TradeOrder.orderNo */
    private String orderNo;
    /** 商品ID，对应 sample_goods.id */
    private Long goodsId;
    /** 下单时的商品名快照 */
    private String goodsName;
    /** 下单时的单价快照 */
    private BigDecimal unitPrice;
    /** 购买数量 */
    private Integer quantity;
    /** unitPrice * quantity，与 TradeOrder.amount 一致 */
    private BigDecimal amount;
    /** 业务方自定义的买家标识，本案例不接用户系统，透传即可 */
    private String buyerId;
    /** 业务自己的状态机：PENDING/PAID/CLOSED/REFUNDED，对应 SampleGoodsOrderStatus 枚举名 */
    private String status;
    /** 支付成功（锁单）时间 */
    private Date payTime;
    /** 创建时间 */
    private Date createTime;
    /** 最后更新时间 */
    private Date updateTime;
}
