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
    String[] properties();

    /**
     * 是否过滤类型的注解，用于在字段或方法返回值里，如果返回值或字段类型的累也有 {@link ExcludeProperties} 注解是否一起过滤
     *
     * @return true 是，否则 false
     */
    boolean filterClassType() default false;

    /**
     * 忽略此注解下的字段
     *
     * @author maurice.chen
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})
    @interface Exclude {

        /**
         * 匹配值，用于针对不同领域的业务响应不同结果的配置使用
         *
         * @return 值
         */
        String value();
    }
}
