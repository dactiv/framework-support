package com.github.dactiv.framework.idempotent.annotation;

import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.idempotent.LockType;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;

import java.lang.annotation.*;

/**
 * 并发处理注解
 *
 * @author maurice
 */
@Documented
@Repeatable(ConcurrentElements.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Concurrent {

    /**
     * 并发的 key
     * <p>spring el 规范: 启用 spring el 时，通过中括号([])识别启用</p>
     * <p>如:</p>
     * <p>@Concurrent(value="[#vo.fieldName]")</p>
     * <p>public void save(Vo vo);</p>
     *
     * @return key 名称
     */
    String value() default "";

    /**
     * 判断条件
     *
     * <p>spring el 规范: 启用 spring el 时，通过中括号([])识别启用</p>
     * <p>如:</p>
     * <p>@Concurrent(condition="[#vo.fieldName] != null")</p>
     * <p>public void save(Vo vo);</p>
     *
     * @return 条件表达式
     */
    String condition() default "";

    /**
     * 异常信息
     *
     * @return 信息
     */
    String exception() default ConcurrentInterceptor.DEFAULT_EXCEPTION;

    /**
     * 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     *
     * @return 锁等待时间
     */
    Time waitTime() default @Time(1000);

    /**
     * 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     *
     * @return 释放锁时间
     */
    Time leaseTime() default @Time(5000);

    /**
     * 锁类型
     *
     * @return 所类型
     */
    LockType type() default LockType.Lock;

}
