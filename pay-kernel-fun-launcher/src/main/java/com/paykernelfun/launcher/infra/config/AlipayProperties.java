package com.paykernelfun.launcher.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝渠道配置，全部来自 application.yml（不落数据库）——
 * 由 {@link #sandbox} 布尔值在代码里选择沙箱/生产网关，避免手填错网关地址。
 */
@Data
@Component
@ConfigurationProperties(prefix = "pay-kernel-fun.alipay")
public class AlipayProperties {
    /** 支付宝开放平台应用 APPID */
    private String appId;
    /** 应用 RSA2 私钥（PKCS8 格式，不含头尾） */
    private String privateKey;
    /** 支付宝公钥（用于验签回调，从开放平台获取） */
    private String alipayPublicKey;
    /** 签名算法，固定 RSA2 */
    private String signType = "RSA2";
    /** 字符集，固定 UTF-8 */
    private String charset = "UTF-8";
    /** 数据格式，固定 json */
    private String format = "json";
    /** 接口内容加密算法，固定 AES（开放平台开启接口加密时才需要） */
    private String encryptType = "AES";
    /** AES 密钥（在支付宝开放平台「接口加密方式」中生成并下载，Base64 格式），不开启加密则留空 */
    private String encryptKey;
    /** 异步回调地址 */
    private String notifyUrl;
    /**
     * 是否沙箱环境：
     * true  -> https://openapi-sandbox.dl.alipaydev.com/gateway.do
     * false -> https://openapi.alipay.com/gateway.do
     */
    private boolean sandbox = true;

    public String getGatewayUrl() {
        return sandbox
                ? "https://openapi-sandbox.dl.alipaydev.com/gateway.do"
                : "https://openapi.alipay.com/gateway.do";
    }
}
