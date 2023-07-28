package com.github.dactiv.framework.spring.web.result.filter.annotation.view;


import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.*;

/**
 * 多个 {@link IncludeView} 的注解
 *
 * @author maurice.chen
 */
@Documented
@JacksonAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface IncludeViews {

    /**
     * {@link IncludeView} 的注解值
     *
     * @return 值
     */
    IncludeView[] value();
}
