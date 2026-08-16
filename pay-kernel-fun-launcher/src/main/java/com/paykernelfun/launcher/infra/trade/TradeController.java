package com.paykernelfun.launcher.infra.trade;

import com.paykernelfun.core.compensation.TradeCloseCompensationQueue;
import com.paykernelfun.core.compensation.TradeQueryCompensationQueue;
import com.paykernelfun.core.model.RawNotification;
import com.paykernelfun.core.model.TradeCreateRequest;
import com.paykernelfun.core.model.TradeCreateResult;
import com.paykernelfun.core.model.TradeOpenRequest;
import com.paykernelfun.core.model.TradeOrder;
import com.paykernelfun.core.registry.ChannelRegistry;
import com.paykernelfun.core.repository.TradeOrderRepository;
import com.paykernelfun.core.spi.biz.TradeOpenHandler;
import com.paykernelfun.core.spi.biz.TradeOpenRejectedException;
import com.paykernelfun.core.spi.channel.TradeChannelException;
import com.paykernelfun.core.spi.channel.TradeCreateHandler;
import com.paykernelfun.core.spi.channel.TradeNotifyHandler;
import com.paykernelfun.core.spi.channel.TradeQueryExecutor;
import com.paykernelfun.core.spi.channel.TradeRefundExecutor;
import com.paykernelfun.launcher.infra.config.TradeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 唯一的交易 HTTP 入口，属于支付基础设施层，不属于任何具体业务。
 *
 * 新增一个业务不需要新增 Controller：业务方只需实现 {@code TradeOpenHandler}/{@code TradeSuccessListener}/
 * {@code TradeRefundListener}/{@code TradeCloseListener} 四个 SPI 并注册为 Spring Bean，下单请求里带上对应的
 * bizType 即可路由过去。新增一个渠道同理，不需要改这个类，只需实现渠道侧 SPI 并把 channel 标识填进请求。
 */
@RestController
@RequiredArgsConstructor
public class TradeController {

    private final ChannelRegistry<TradeOpenHandler> tradeOpenHandlerRegistry;
    private final ChannelRegistry<TradeCreateHandler> tradeCreateHandlerRegistry;
    private final ChannelRegistry<TradeNotifyHandler> tradeNotifyHandlerRegistry;
    private final ChannelRegistry<TradeQueryExecutor> tradeQueryExecutorRegistry;
    private final ChannelRegistry<TradeRefundExecutor> tradeRefundExecutorRegistry;
    private final TradeOrderRepository tradeOrderRepository;
    private final TradeOrderCloser tradeOrderCloser;
    private final TradeQueryCompensationQueue queryCompensationQueue;
    private final TradeCloseCompensationQueue closeCompensationQueue;
    private final TradeProperties tradeProperties;

    @PostMapping("/trade/orders")
    public CreateTradeResponse createOrder(@RequestBody CreateTradeRequest req) {
        TradeOpenRequest openRequest = new TradeOpenRequest();
        openRequest.setBizOrderNo(req.getBizOrderNo());
        openRequest.setBizType(req.getBizType());
        openRequest.setChannel(req.getChannel());
        openRequest.setAmount(req.getAmount());
        openRequest.setExtra(req.getExtra());

        TradeOrder order = tradeOpenHandlerRegistry.require(req.getBizType()).buildOrder(openRequest);
        Date expireTime = new Date(System.currentTimeMillis() + tradeProperties.getOrderValiditySeconds() * 1000);
        order.setExpireTime(expireTime);
        tradeOrderRepository.save(order);

        TradeCreateRequest createRequest = new TradeCreateRequest();
        createRequest.setOrder(order);
        createRequest.setSubject(order.getBizType() + "-" + order.getBizOrderNo());
        createRequest.setReturnUrl(tradeProperties.getReturnUrl());

        TradeCreateResult result = tradeCreateHandlerRegistry.require(order.getChannel()).create(createRequest);

        // 下单成功后立即挂号到两个补偿队列：查询补偿负责弥补回调丢失，关单补偿负责到期收尾。
        // 两条队列语义独立，谁先触发谁负责把订单从另一条队列摘除（见 TradePaymentConfirmer / TradeCloseCompensationTask）。
        Date firstCheckAt = new Date(System.currentTimeMillis() + tradeProperties.getQueryRetryIntervalSeconds() * 1000);
        queryCompensationQueue.upsert(order.getOrderNo(), firstCheckAt);
        closeCompensationQueue.upsert(order.getOrderNo(), expireTime);

        return new CreateTradeResponse(order.getOrderNo(), result.getPayload());
    }

    @PostMapping(value = "/trade/notify/{channel}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String notify(@PathVariable String channel, HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, values.length > 0 ? values[0] : null));
        RawNotification notification = new RawNotification(channel, params, null);
        tradeNotifyHandlerRegistry.require(channel).handleNotify(notification);
        return "success";
    }

    @GetMapping("/trade/orders/{orderNo}")
    public TradeOrder getOrder(@PathVariable String orderNo) {
        return tradeOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("order not found: " + orderNo));
    }

    /**
     * 用户在渠道支付页完成操作后浏览器同步跳转到这里（{@link TradeProperties#getReturnUrl()} 配的就是这个地址）。
     * 同步跳转只表示用户在渠道侧完成了操作，不代表支付一定成功——真正的支付结果以异步通知
     * （{@link #notify}）或补偿查询任务为准，这里只是把当前已知的订单状态展示给用户。
     */
    @GetMapping("/trade/return")
    public TradeOrder handleReturn(@RequestParam("out_trade_no") String orderNo) {
        return getOrder(orderNo);
    }

    @GetMapping("/trade/orders/{orderNo}/remote-status")
    public Object getRemoteStatus(@PathVariable String orderNo) {
        TradeOrder order = getOrder(orderNo);
        return tradeQueryExecutorRegistry.require(order.getChannel()).queryDetail(orderNo);
    }

    @PostMapping("/trade/orders/{orderNo}/refund")
    public Map<String, String> refund(@PathVariable String orderNo,
                                       @RequestParam(defaultValue = "案例退款") String reason) {
        TradeOrder order = getOrder(orderNo);
        String channelRefundId = tradeRefundExecutorRegistry.require(order.getChannel())
                .refund(orderNo, order.getAmount(), reason);
        return Collections.singletonMap("channelRefundId", channelRefundId);
    }

    @PostMapping("/trade/orders/{orderNo}/close")
    public Map<String, Boolean> close(@PathVariable String orderNo) {
        TradeOrder order = getOrder(orderNo);
        boolean closed = tradeOrderCloser.close(order);
        return Collections.singletonMap("closed", closed);
    }

    @ExceptionHandler(TradeOpenRejectedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleOpenRejected(TradeOpenRejectedException e) {
        return Collections.singletonMap("message", e.getMessage());
    }

    /** 渠道明确拒绝了这次调用（如"交易不存在"）或调用异常，把渠道原始错误信息带回去，不让调用方看到裸的 500。 */
    @ExceptionHandler(TradeChannelException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleChannelException(TradeChannelException e) {
        return Collections.singletonMap("message", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(IllegalArgumentException e) {
        return Collections.singletonMap("message", e.getMessage());
    }
}
