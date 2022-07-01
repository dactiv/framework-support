package com.github.dactiv.framework.idempotent.annotation;

import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;

import java.lang.annotation.*;

/**
 * 幂等性注解
 *
 * @author maurice
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 锁识别，用于记录当前使用什么识别去启用幂等行控制。
     *
     * <p>spring el 规范: 启用 spring el 时，通过中括号([])识别启用</p>
     * <p>如:</p>
     * <p>@Idempoten(key="[#vo.fieldName]")</p>
     * <p>public void save(Vo vo);</p>
     *
     * @return 锁识别
     */
    String key() default "";

    /**
     * 值，用于记录当前使用什么值去记录幂等行的断言
     * <p>spring el 规范: 启用 spring el 时，通过中括号([])识别启用</p>
     * <p>如:</p>
     * <p>@Idempoten(value="[#vo.fieldName]")</p>
     * <p>public void save(Vo vo);</p>
     *
     * @return 锁识别
     */
    String[] value() default {};

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
     * 异常信息，如果通过锁识别查询出数据信息，提示的异常是信息是什么。
     *
     * @return 信息
     */
    String exception() default ConcurrentInterceptor.DEFAULT_EXCEPTION;

    /**
     * 如果不使用 {@link #value()} 时，要忽略哪个参数不做 hashCode
     *
     * @return 要忽略的参数
     */
    String[] ignore() default {};

    /**
     * 过期时间，用于说明间隔提交时间在什么范围内能在此提交
     *
     * @return 时间
     */
    Time expirationTime() default @Time(3000);

}
