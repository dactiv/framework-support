package com.github.dactiv.framework.commons.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 时间注解
 *
 * @author maurice.chen
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Time {

    /**
     * 时间值
     *
     * @return 值
     */
    long value();

    /**
     * 时间单位
     *
     * @return 时间单位
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

}
