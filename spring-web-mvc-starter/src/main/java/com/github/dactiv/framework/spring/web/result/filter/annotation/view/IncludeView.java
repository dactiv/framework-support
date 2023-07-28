package com.github.dactiv.framework.spring.web.result.filter.annotation.view;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.*;

/**
 * 仅仅引入属性视图注解（该注解无法和 {@link ExcludeView} 共存， 如果配置了 {@link ExcludeView}，优先执行 {@link ExcludeView}），
 * 用于 jackson json 序列化 json 时，通过该注解来指定使用哪个方式进行序列化。
 *
 * @author maurice.chen
 */
@Documented
@JacksonAnnotation
@Repeatable(IncludeViews.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface IncludeView {

    /**
     * 明细视图
     */
    String DETAIL_VIEW = "detail_inc_view";

    /**
     * 集合视图
     */
    String LIST_VIEW = "list_inc_view";

    /**
     * id 名称视图值
     */
    String ID_NAME_VIEW = "id_name_inc_view";

    /**
     * 匹配值，用于针对不同领域的业务响应不同结果的配置使用
     *
     * @return 值
     */
    String value() default "";

    /**
     * 要过滤的属性名称
     *
     * @return 名称数组
     */
    String[] properties();
}
