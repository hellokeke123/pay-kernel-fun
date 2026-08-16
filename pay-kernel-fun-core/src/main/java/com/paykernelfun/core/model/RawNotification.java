package com.paykernelfun.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 渠道异步回调的原始载荷，具体渠道自行解析（表单参数、JSON body 等），core 不假设格式。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawNotification {
    /** 渠道标识，如 ALIPAY */
    private String channel;
    /** 表单参数形式的回调数据（如支付宝异步通知） */
    private Map<String, String> params;
    /** JSON body 形式的回调数据（如某些渠道用 JSON 推送），与 params 二选一使用，具体看渠道协议 */
    private String rawBody;
}
