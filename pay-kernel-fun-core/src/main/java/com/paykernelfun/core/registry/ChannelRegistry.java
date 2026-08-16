package com.paykernelfun.core.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按 key（渠道标识或业务类型）建路由表的通用注册表。
 * 用法：Spring 注入 {@code List<T>} 收集所有实现，构造一个 {@code ChannelRegistry<T>} Bean 即可。
 */
public class ChannelRegistry<T> {

    private final Map<String, T> byKey;

    public ChannelRegistry(List<T> handlers, Function<T, String> keyFn) {
        this.byKey = handlers.stream().collect(Collectors.toMap(keyFn, Function.identity()));
    }

    public Optional<T> find(String key) {
        return Optional.ofNullable(byKey.get(key));
    }

    public T require(String key) {
        T handler = byKey.get(key);
        if (handler == null) {
            throw new IllegalStateException("No handler registered for key: " + key);
        }
        return handler;
    }
}
