package com.github.dactiv.framework.spring.web.result.filter.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.spring.web.result.filter.FilterPropertyExecutor;
import com.github.dactiv.framework.spring.web.result.filter.annotation.ExcludeProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * jackson json 实现过滤属性执行器
 *
 * @author maurice.chen
 *
 */
public class JacksonFilterPropertyExecutor implements FilterPropertyExecutor {

    private ObjectMapper objectMapper = new ObjectMapper();

    public JacksonFilterPropertyExecutor() {
    }

    public JacksonFilterPropertyExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object filter(String id, Object data) {

        ExcludeProperties excludeProperties = AnnotationUtils.findAnnotation(data.getClass(), ExcludeProperties.class);

        if (Objects.isNull(excludeProperties)) {
            return data;
        }

        if (!excludeProperties.value().equals(id)) {
            return data;
        }

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(data.getClass());

        if (ArrayUtils.isEmpty(propertyDescriptors)) {
            return data;
        }

        //noinspection unchecked
        Map<String, Object> returnValue = objectMapper.convertValue(data, Map.class);

        for (PropertyDescriptor descriptor : propertyDescriptors) {

            Field field = ReflectionUtils.findField(data.getClass(), descriptor.getName());

            if (Objects.nonNull(field)) {

                filterFieldOrMethod(id, field, descriptor.getName(), returnValue);

            }

            Method readMethod = descriptor.getReadMethod();

            if (Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {

                filterFieldOrMethod(id, readMethod, descriptor.getName(), returnValue);
            }

        }

        for (String property : excludeProperties.properties()) {
            returnValue.remove(property);
        }

        return returnValue;
    }

    private void filterFieldOrMethod(String id, Object target, String name, Map<String, Object> returnValue) {

        ExcludeProperties excludeProperties;

        ExcludeProperties.Exclude exclude;

        if (Field.class.isAssignableFrom(target.getClass())) {
            Field value = Casts.cast(target);
            excludeProperties = AnnotationUtils.findAnnotation(value, ExcludeProperties.class);
            exclude = AnnotationUtils.findAnnotation(value, ExcludeProperties.Exclude.class);
        } else if (Method.class.isAssignableFrom(target.getClass())) {
            Method value = Casts.cast(target);
            excludeProperties = AnnotationUtils.findAnnotation(value, ExcludeProperties.class);
            exclude = AnnotationUtils.findAnnotation(value, ExcludeProperties.Exclude.class);
        } else {
            throw new ServiceException("不支持 [" + target.getClass().getName() + "] 的过滤");
        }

        List<String> filterProperties = new LinkedList<>();

        if (Objects.nonNull(excludeProperties) && excludeProperties.value().equals(id)) {

            filterProperties.addAll(Arrays.stream(excludeProperties.properties()).collect(Collectors.toList()));
        }

        if (Objects.nonNull(exclude) && exclude.value().equals(id)) {
            returnValue.remove(name);
        }

        if (CollectionUtils.isNotEmpty(filterProperties)) {
            Object value = returnValue.get(name);

            if (Objects.nonNull(value)) {
                Object valueResult = filter(value, filterProperties);
                returnValue.put(name, valueResult);
            }
        }
    }

    private Object filter(Object value, List<String> properties) {

        if (List.class.isAssignableFrom(value.getClass())) {
            List<Object> list = Casts.cast(value);
            return list.stream().map(o -> filter(o, properties)).collect(Collectors.toList());
        }

        Map<String, Object> data;

        if (Map.class.isAssignableFrom(value.getClass())) {
            data = Casts.cast(value);
        } else {
            //noinspection unchecked
            data = Casts.convertValue(value, Map.class);
        }

        properties.forEach(data::remove);

        return data;
    }
}
