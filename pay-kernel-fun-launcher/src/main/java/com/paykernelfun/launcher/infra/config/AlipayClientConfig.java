package com.paykernelfun.launcher.infra.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class AlipayClientConfig {

    private final AlipayProperties properties;

    @Bean
    public AlipayClient alipayClient() {
        if (StringUtils.hasText(properties.getEncryptKey())) {
            return new DefaultAlipayClient(
                    properties.getGatewayUrl(),
                    properties.getAppId(),
                    properties.getPrivateKey(),
                    properties.getFormat(),
                    properties.getCharset(),
                    properties.getAlipayPublicKey(),
                    properties.getSignType(),
                    properties.getEncryptKey(),
                    properties.getEncryptType());
        }
        return new DefaultAlipayClient(
                properties.getGatewayUrl(),
                properties.getAppId(),
                properties.getPrivateKey(),
                properties.getFormat(),
                properties.getCharset(),
                properties.getAlipayPublicKey(),
                properties.getSignType());
    }
}
