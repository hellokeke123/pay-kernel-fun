package com.paykernelfun.core.exception;

import lombok.Data;

import java.util.Date;

@Data
public class TradeExceptionRecord {
    /** 内部交易流水号，可为空（如订单还未创建就出错的场景） */
    private String orderNo;
    /** 支付渠道标识 */
    private String channel;
    /** 出现异常的场景，如 notify/refund/close，用于排查时快速定位 */
    private String scene;
    /** 异常描述信息 */
    private String message;
    /** 记录发生时间 */
    private Date occurredAt = new Date();

    public static class Builder {
        private final TradeExceptionRecord record = new TradeExceptionRecord();

        public Builder orderNo(String orderNo) {
            record.setOrderNo(orderNo);
            return this;
        }

        public Builder channel(String channel) {
            record.setChannel(channel);
            return this;
        }

        public Builder scene(String scene) {
            record.setScene(scene);
            return this;
        }

        public Builder message(String message) {
            record.setMessage(message);
            return this;
        }

        public TradeExceptionRecord build() {
            return record;
        }
    }
}
