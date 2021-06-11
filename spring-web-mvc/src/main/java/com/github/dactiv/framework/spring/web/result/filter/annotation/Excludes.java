package com.github.dactiv.framework.spring.web.result.filter.annotation;


import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.*;

/**
 * 多个 {@link Exclude} 的注解
 *
 * @author maurice.chen
 */
@Documented
@JacksonAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Excludes {

    /**
     * {@link Exclude} 的注解值
     *
     * @return 值
     */
    Exclude[] value();
}
