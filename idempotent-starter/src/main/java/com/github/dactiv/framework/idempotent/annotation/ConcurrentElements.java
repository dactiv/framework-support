package com.github.dactiv.framework.idempotent.annotation;

import java.lang.annotation.*;

/**
 * 多并发注解
 *
 * @author maurice.chen
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConcurrentElements {

    /**
     * 并发注解数组
     *
     * @return 并发注解数据
     */
    Concurrent[] value();

}
