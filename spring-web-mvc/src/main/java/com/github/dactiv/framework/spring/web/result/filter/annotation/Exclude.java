package com.github.dactiv.framework.spring.web.result.filter.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.*;

/**
 * 过滤字段或方法注解，用于 http 响应数据时过滤响应的 json 结构
 *
 * @author maurice.chen
 */
@Documented
@JacksonAnnotation
@Repeatable(Excludes.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Exclude {

    /**
     * 匹配值，用于针对不同领域的业务响应不同结果的配置使用
     *
     * @return 值
     */
    String value();
}
