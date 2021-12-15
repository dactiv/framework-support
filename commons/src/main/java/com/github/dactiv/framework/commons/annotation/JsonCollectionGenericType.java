package com.github.dactiv.framework.commons.annotation;


import java.lang.annotation.*;

/**
 * json 集合泛型类型声明注解
 *
 * @author maurice.chen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface JsonCollectionGenericType {

    /**
     * 泛型类型
     *
     * @return 泛型类型 class
     */
    Class<?> value();

}
