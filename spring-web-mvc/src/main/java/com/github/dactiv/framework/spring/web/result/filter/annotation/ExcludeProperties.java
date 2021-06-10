package com.github.dactiv.framework.spring.web.result.filter.annotation;

import java.lang.annotation.*;

/**
 * 过滤属性注解，用于 http 响应数据时过滤响应的 json 结构
 *
 * @author maurice.chen
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface ExcludeProperties {

    /**
     * 匹配值，用于针对不同领域的业务响应不同结果的配置使用
     *
     * @return 值
     */
    String value();

    /**
     * 要过滤的属性名称
     *
     * @return 名称数组
     */
    String[] properties() default {};
}
