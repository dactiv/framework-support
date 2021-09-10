package com.github.dactiv.framework.idempotent.annotation;

import com.github.dactiv.framework.commons.annotation.Time;

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
     * @return 锁识别
     */
    String[] value() default {};

    /**
     * 异常信息，如果通过锁识别查询出数据信息，提示的异常是信息是什么。
     *
     * @return 信息
     */
    String exception() default "请不要重复操作";

    /**
     * 过期时间，用于说明间隔提交时间在什么范围内能在此提交
     *
     * @return 时间
     */
    Time expirationTime() default @Time(5000);

}
