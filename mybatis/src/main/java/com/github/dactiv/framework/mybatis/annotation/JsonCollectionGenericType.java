package com.github.dactiv.framework.mybatis.annotation;


import java.lang.annotation.*;

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
