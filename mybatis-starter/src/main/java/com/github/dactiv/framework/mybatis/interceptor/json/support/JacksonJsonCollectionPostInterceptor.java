package com.github.dactiv.framework.mybatis.interceptor.json.support;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.mybatis.handler.NameValueEnumTypeHandler;
import com.github.dactiv.framework.mybatis.interceptor.json.AbstractJsonCollectionPostInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * jackson json 实现的 json 集合后续映射拦截器
 *
 * @author maurice.chen
 */
@Intercepts(
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
)
public class JacksonJsonCollectionPostInterceptor extends AbstractJsonCollectionPostInterceptor {

    @Override
    protected Object doMappingResult(Object result, Class<?> type, List<PropertyDescriptor> propertyDescriptors) {
        for (PropertyDescriptor pd : propertyDescriptors) {
            Class<?> collectionClass = null;

            Field field = ReflectionUtils.findFiled(result, pd.getName());
            if (Objects.nonNull(field)) {
                collectionClass = field.getType();
            }

            Method method = pd.getReadMethod();
            if (Objects.nonNull(method)) {
                collectionClass = pd.getReadMethod().getReturnType();
            }

            if (Objects.isNull(collectionClass)) {
                continue;
            }

            Object value = ReflectionUtils.getFieldValue(result, pd.getName());
            if(Objects.isNull(value)) {
                continue;
            }

            JsonCollectionGenericType ann = getJsonCollectionGenericType(pd, type);
            Class<?> targetClass = ann.value();

            Object newValue;

            if (ValueEnum.class.isAssignableFrom(targetClass) || NameEnum.class.isAssignableFrom(targetClass)) {
                Collection<?> collection = Casts.cast(value);
                Stream<Object> stream = collection
                        .stream()
                        .map(o -> getEnumValue(o, targetClass))
                        .filter(Objects::nonNull);
                if (List.class.equals(collectionClass)) {
                    newValue = stream.collect(Collectors.toList());
                } else if (Set.class.equals(collectionClass)) {
                    newValue = stream.collect(Collectors.toSet());
                } else {
                    throw new SystemException("找不到对应类型为 [" + collectionClass + "] 的集合处理方式");
                }
            } else {
                CollectionType collectionType = TypeFactory
                        .defaultInstance()
                        .constructCollectionType(Casts.cast(collectionClass), targetClass);
                newValue = Casts.convertValue(value, collectionType);
            }

            if (Objects.nonNull(pd.getWriteMethod())) {
                ReflectionUtils.invokeMethod(result, pd.getWriteMethod(), Collections.singletonList(newValue));
            } else {
                ReflectionUtils.setFieldValue(result, pd.getName(), newValue);
            }
        }
        return result;
    }

    private Object getEnumValue(Object o, Class<?> targetClass) {
        Object result = NameValueEnumTypeHandler.getValue(o, targetClass);

        if (Objects.isNull(result)) {
            result = Enum.valueOf(Casts.cast(targetClass), o.toString());
        }

        return result;
    }
}
