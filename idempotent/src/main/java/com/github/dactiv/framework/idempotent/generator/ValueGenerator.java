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

    /**
     * 断言条件
     *
     * @param condition 断言结果
     * @param method 被调用的方法
     * @param args   参数值
     *
     * @return true 断言成功，否则失败
     */
    boolean assertCondition(String condition, Method method, Object... args);
}
