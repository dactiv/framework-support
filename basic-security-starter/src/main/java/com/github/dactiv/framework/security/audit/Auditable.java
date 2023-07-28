package com.github.dactiv.framework.security.audit;

import java.lang.annotation.*;

/**
 * 需审计的内容注解
 *
 * @author maurice
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * 获取当事人值的属性名称
     *
     * <p>
     * 如：
     * request 提交的参数或头信息存在 token=test，要获取 token 做 principal 的话，principal 就等于 token
     * </p>
     *
     * @return 当事人值的属性名称
     */
    String principal() default "";

    /**
     * 审计类型
     *
     * @return 类型
     */
    String type();

    /**
     * 是否操作数据留痕
     *
     * @return true 是，否则 false
     */
    boolean operationDataTrace() default false;

}
