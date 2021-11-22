package com.github.dactiv.framework.mybatis.interceptor;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.mybatis.annotation.JsonCollectionGenericType;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public abstract class JsonCollectionPropertyPostInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object result = invocation.proceed();

        MappedStatement mappedStatement = Casts.cast(invocation.getArgs()[0]);

        List<Class<?>> types = mappedStatement
                .getResultMaps()
                .stream()
                .map(ResultMap::getType)
                .filter(this::hasJsonCollectionProperties)
                .collect(Collectors.toList());

        return mappingCollectionProperty(result, types);
    }

    private Object mappingCollectionProperty(Object result, List<Class<?>> types) {
        if (!Collection.class.isAssignableFrom(result.getClass())) {
            return result;
        }
        return doMappingCollectionProperty(result, types);
    }

    protected abstract Object doMappingCollectionProperty(Object result, List<Class<?>> types);

    private boolean hasJsonCollectionProperties(Class<?> targetClass) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(targetClass);
        return Arrays
                .stream(propertyDescriptors)
                .anyMatch(p -> this.hasJsonCollectionGenericType(p, targetClass));
    }

    private boolean hasJsonCollectionGenericType(PropertyDescriptor propertyDescriptor, Class<?> targetClass) {
        boolean hasMethod = false;
        Method method = propertyDescriptor.getReadMethod();
        if (Objects.nonNull(method)) {
            hasMethod = AnnotatedElementUtils.findMergedAnnotation(method, JsonCollectionGenericType.class) != null;
        }

        boolean hasField = false;
        Field field = FieldUtils.getDeclaredField(targetClass, propertyDescriptor.getName(), true);
        if (Objects.nonNull(field)) {
            hasField = AnnotatedElementUtils.findMergedAnnotation(field, JsonCollectionGenericType.class) != null;
        }

        return hasMethod || hasField;
    }

}
