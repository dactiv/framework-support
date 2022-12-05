package com.github.dactiv.framework.commons.annotation;

import java.lang.annotation.*;

/**
 * 取值策略
 *
 * @author maurice.chen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface GetValueStrategy {

    /**
     * 取值类型
     *
     * @return 取值类型
     */
    Type type() default Type.Value;

    /**
     * 取值类型
     *
     * @author maurice.chen
     */
    enum Type {

        /**
         * 值
         */
        Value,

        /**
         * 名称
         */
        Name,

        /**
         * toString 方法
         */
        ToString

    }
}
