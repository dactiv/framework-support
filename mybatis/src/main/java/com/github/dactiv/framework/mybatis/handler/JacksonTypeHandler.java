package com.github.dactiv.framework.mybatis.handler;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;

/**
 * jackson json type handler 实现
 *
 * @param <T> json 实体类型 class
 *
 * @author maurice.chen
 */
public class JacksonTypeHandler<T> extends AbstractJsonTypeHandler<T>{

    private final Class<T> entityClass;

    public JacksonTypeHandler() {
        this.entityClass = ReflectionUtils.getGenericClass(this, 0);
    }

    public JacksonTypeHandler(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    protected T read(String json) {
        return Casts.readValue(json, entityClass);
    }

    @Override
    protected String write(T obj) {
        return Casts.writeValueAsString(obj);
    }
}
