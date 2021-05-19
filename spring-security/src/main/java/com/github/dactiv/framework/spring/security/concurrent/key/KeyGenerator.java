package com.github.dactiv.framework.spring.security.concurrent.key;

import org.aopalliance.intercept.MethodInvocation;

/**
 * key 生成器
 *
 * @author maurice.chen
 */
public interface KeyGenerator {

    /**
     * 生成 key
     *
     * @param key        当前 key
     * @param invocation 方法调用
     *
     * @return key 实际值
     */
    String generate(String key, MethodInvocation invocation);
}
