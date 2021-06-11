package com.github.dactiv.framework.spring.web.result.filter.holder.strategy;

import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolderStrategy;

import java.util.Objects;

/**
 * Thread Local 的 socket 结果集持有者实现
 *
 * @author maurice.chen
 */
public class ThreadLocalFilterResultHolderStrategy implements FilterResultHolderStrategy {

    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public void clear() {
        THREAD_LOCAL.remove();
    }

    @Override
    public String get() {
        return THREAD_LOCAL.get();
    }

    @Override
    public void set(String id) {
        Objects.requireNonNull(id,"filter result id 不能为空");
        THREAD_LOCAL.set(id);
    }
}
