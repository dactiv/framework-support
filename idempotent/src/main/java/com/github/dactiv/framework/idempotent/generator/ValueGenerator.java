package com.github.dactiv.framework.idempotent.generator;

import java.lang.reflect.Method;

/**
 * key 生成器
 *
 * @author maurice.chen
 */
public interface ValueGenerator {

    /**
     * 生成 key
     *
     * @param key    当前 key
     * @param method 被调用的方法
     * @param args 参数值
     * @return key 实际值
     */
    Object generate(String key, Method method, Object... args);
}
