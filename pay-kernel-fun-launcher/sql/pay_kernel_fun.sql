CREATE DATABASE IF NOT EXISTS pay_kernel_fun_db DEFAULT CHARACTER SET utf8mb4;

USE pay_kernel_fun_db;

CREATE TABLE IF NOT EXISTS trade_order (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no         VARCHAR(64)  NOT NULL COMMENT '内部交易流水号',
    biz_order_no     VARCHAR(64)  NOT NULL COMMENT '业务方订单号',
    biz_type         VARCHAR(32)  NOT NULL COMMENT '业务类型，如 SAMPLE_GOODS',
    channel          VARCHAR(32)  NOT NULL COMMENT '支付渠道，如 ALIPAY',
    amount           DECIMAL(12, 2) NOT NULL COMMENT '金额',
    currency         VARCHAR(8)   NOT NULL DEFAULT 'CNY',
    status           VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/CLOSED/REFUNDED',
    channel_trade_no VARCHAR(64)  DEFAULT NULL COMMENT '渠道侧交易号',
    extra            TEXT COMMENT '渠道自定义字段(JSON)',
    pay_time         DATETIME     DEFAULT NULL,
    refund_time      DATETIME     DEFAULT NULL,
    expire_time      DATETIME     DEFAULT NULL COMMENT '订单有效期截止时间，供到期自动关单任务使用',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_biz_order_no (biz_order_no),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='一次性交易订单（pay-kernel-fun-core 抽象对应表），案例应用 pay-kernel-fun-launcher 使用';

-- 示例业务：商品，pay-kernel-fun-launcher 的 biz/sample 使用，与 pay-kernel-fun-core 抽象无关
CREATE TABLE IF NOT EXISTS sample_goods (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_name  VARCHAR(128)   NOT NULL,
    price       DECIMAL(12, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0 COMMENT '可售库存，下单预扣、关单/退款回滚',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='示例业务：商品';

-- 示例业务：商品订单，记录谁在什么时候支付了哪个商品；状态机是业务自己的，不复用 trade_order 的 TradeStatus
CREATE TABLE IF NOT EXISTS sample_goods_order (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no    VARCHAR(64)   NOT NULL COMMENT '对应 trade_order.order_no',
    goods_id    BIGINT        NOT NULL,
    goods_name  VARCHAR(128)  NOT NULL COMMENT '下单时的商品名快照',
    unit_price  DECIMAL(12, 2) NOT NULL COMMENT '下单时的单价快照',
    quantity    INT           NOT NULL,
    amount      DECIMAL(12, 2) NOT NULL COMMENT 'unit_price * quantity，与 trade_order.amount 一致',
    buyer_id    VARCHAR(64)   NOT NULL COMMENT '业务方自定义的买家标识，本案例不接用户系统，透传即可',
    status      VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/CLOSED/REFUNDED，业务自己的状态机',
    pay_time    DATETIME      DEFAULT NULL COMMENT '支付成功（锁单）时间',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_goods_id (goods_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='示例业务：商品订单，记录谁在什么时候支付了哪个商品';

