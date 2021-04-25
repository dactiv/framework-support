package com.github.dactiv.framework.spring.security.concurrent.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 并发处理注解
 *
 * @author maurice
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConcurrentProcess {

    /**
     * 并发的 key
     * <p>
     * @return key 名称
     */
    String value();

    /**
     * 异常信息
     *
     * @return 信息
     */
    String exceptionMessage() default "请不要重复操作";

    /**
     * 锁等待时间
     *
     * @return 锁等待时间
     */
    long waitTime() default -1;

    /**
     * 锁生存/释放时间
     *
     * @return 锁生存/释放时间
     */
    long leaseTime() default 1000;

    /**
     * 时间单位
     *
     * @return 时间单位
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

}
