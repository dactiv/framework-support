package com.github.dactiv.framework.idempotent.generator;

import java.lang.reflect.Method;

/**
 * key 生成器
 *
 * @author maurice.chen
 */
public interface ValueGenerator {

    /**
     * 生成值
     *
     * @param token  当前 token
     * @param method 被调用的方法
     * @param args   参数值
     *
     * @return 实际值
     */
    Object generate(String token, Method method, Object... args);
}
