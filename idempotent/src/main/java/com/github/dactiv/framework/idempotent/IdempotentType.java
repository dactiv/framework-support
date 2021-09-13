package com.github.dactiv.framework.idempotent;

/**
 * 幂等类型，用于在配置时候的自动注入使用。
 *
 * @author maurice.chen
 */
public enum IdempotentType {
    /**
     * aop 切面
     */
    Advisor,
    /**
     * spring mvc 拦截器
     */
    Interceptor;
}
