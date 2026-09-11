-- pay-kernel-fun-subscription 建表 SQL
-- 订阅模块只提供抽象与模型，案例应用不接线运行；接入方按需实现并建表。

CREATE TABLE IF NOT EXISTS subscription_record (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscription_no  VARCHAR(64) NOT NULL COMMENT '订阅号',
    biz_user_id      VARCHAR(64) NOT NULL COMMENT '业务方用户标识',
    channel          VARCHAR(32) NOT NULL,
    product_id       VARCHAR(64) NOT NULL,
    status           VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'ACTIVE/EXPIRED/CANCELLED/PENDING',
    period_start     DATETIME    DEFAULT NULL,
    period_end       DATETIME    DEFAULT NULL,
    auto_renew       TINYINT(1)  NOT NULL DEFAULT 0,
    create_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_subscription_no (subscription_no)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订阅记录（pay-kernel-fun-subscription 抽象对应表）';
