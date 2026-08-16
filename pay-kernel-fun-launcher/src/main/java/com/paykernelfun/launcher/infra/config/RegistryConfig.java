package com.paykernelfun.launcher.infra.config;

import com.paykernelfun.core.exception.DefaultLoggingTradeExceptionRecorder;
import com.paykernelfun.core.exception.TradeExceptionRecorder;
import com.paykernelfun.core.registry.ChannelRegistry;
import com.paykernelfun.core.spi.biz.TradeCloseListener;
import com.paykernelfun.core.spi.biz.TradeOpenHandler;
import com.paykernelfun.core.spi.biz.TradeRefundListener;
import com.paykernelfun.core.spi.biz.TradeSuccessListener;
import com.paykernelfun.core.spi.channel.TradeCloseExecutor;
import com.paykernelfun.core.spi.channel.TradeCreateHandler;
import com.paykernelfun.core.spi.channel.TradeNotifyHandler;
import com.paykernelfun.core.spi.channel.TradeQueryExecutor;
import com.paykernelfun.core.spi.channel.TradeRefundExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 把 Spring 自动收集到的各渠道/业务实现，按 channel()/bizType() 建成路由表。
 * 新增一个渠道或一个业务，只要实现对应 SPI 接口并交给 Spring 管理，这里不需要改动。
 */
@Configuration
public class RegistryConfig {

    @Bean
    public ChannelRegistry<TradeCreateHandler> tradeCreateHandlerRegistry(List<TradeCreateHandler> handlers) {
        return new ChannelRegistry<>(handlers, TradeCreateHandler::channel);
    }

    @Bean
    public ChannelRegistry<TradeNotifyHandler> tradeNotifyHandlerRegistry(List<TradeNotifyHandler> handlers) {
        return new ChannelRegistry<>(handlers, TradeNotifyHandler::channel);
    }

    @Bean
    public ChannelRegistry<TradeQueryExecutor> tradeQueryExecutorRegistry(List<TradeQueryExecutor> handlers) {
        return new ChannelRegistry<>(handlers, TradeQueryExecutor::channel);
    }

    @Bean
    public ChannelRegistry<TradeRefundExecutor> tradeRefundExecutorRegistry(List<TradeRefundExecutor> handlers) {
        return new ChannelRegistry<>(handlers, TradeRefundExecutor::channel);
    }

    @Bean
    public ChannelRegistry<TradeCloseExecutor> tradeCloseExecutorRegistry(List<TradeCloseExecutor> handlers) {
        return new ChannelRegistry<>(handlers, TradeCloseExecutor::channel);
    }

    @Bean
    public ChannelRegistry<TradeSuccessListener> tradeSuccessListenerRegistry(List<TradeSuccessListener> listeners) {
        return new ChannelRegistry<>(listeners, TradeSuccessListener::bizType);
    }

    @Bean
    public ChannelRegistry<TradeRefundListener> tradeRefundListenerRegistry(List<TradeRefundListener> listeners) {
        return new ChannelRegistry<>(listeners, TradeRefundListener::bizType);
    }

    @Bean
    public ChannelRegistry<TradeCloseListener> tradeCloseListenerRegistry(List<TradeCloseListener> listeners) {
        return new ChannelRegistry<>(listeners, TradeCloseListener::bizType);
    }

    @Bean
    public ChannelRegistry<TradeOpenHandler> tradeOpenHandlerRegistry(List<TradeOpenHandler> handlers) {
        return new ChannelRegistry<>(handlers, TradeOpenHandler::bizType);
    }

    @Bean
    @ConditionalOnMissingBean(TradeExceptionRecorder.class)
    public TradeExceptionRecorder tradeExceptionRecorder() {
        return new DefaultLoggingTradeExceptionRecorder();
    }
}
