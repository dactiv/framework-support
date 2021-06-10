package com.github.dactiv.framework.spring.web.result.filter.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.spring.web.result.filter.FilterPropertyExecutor;
import com.github.dactiv.framework.spring.web.result.filter.annotation.ExcludeProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 简单的过滤属性执行器实现
 *
 * @author maurice.chen
 *
 */
public class SimpleFilterPropertyExecutor implements FilterPropertyExecutor {

    public SimpleFilterPropertyExecutor() {
    }

    @Override
    public Object filter(String id, Object data) {

        if (Objects.isNull(data)) {
            return null;
        }

        List<String> properties = new LinkedList<>();

        ExcludeProperties excludeProperties = AnnotationUtils.findAnnotation(data.getClass(), ExcludeProperties.class);

        if (Objects.nonNull(excludeProperties) && excludeProperties.value().equals(id)) {
            properties.addAll(Arrays.stream(excludeProperties.properties()).collect(Collectors.toList()));
        }

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(data.getClass());

        if (ArrayUtils.isEmpty(propertyDescriptors)) {
            return data;
        }

        Map<String, Object> returnValue = new LinkedHashMap<>();

        for (PropertyDescriptor descriptor : propertyDescriptors) {

            if (descriptor.getPropertyType().equals(Class.class) || properties.contains(descriptor.getName())) {
                continue;
            }

            Field field = ReflectionUtils.findFiled(data, descriptor.getName());

            if (Objects.isNull(field)) {
                continue;
            }

            ExcludeProperties fieldExclude = AnnotationUtils.findAnnotation(field, ExcludeProperties.class);

            if (Objects.nonNull(fieldExclude) && fieldExclude.value().equals(id)) {

                List<String> fieldProperties = Arrays.stream(fieldExclude.properties()).collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(fieldProperties)) {

                    Object value = ReflectionUtils.getFieldValue(field, data);
                    returnValue.put(descriptor.getName(), getPropertyValue(id, value, fieldProperties));
                }

                continue;

            } else {

                Method readMethod = descriptor.getReadMethod();

                if (Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {

                    ExcludeProperties methodExclude = AnnotationUtils.findAnnotation(readMethod, ExcludeProperties.class);

                    if (Objects.nonNull(methodExclude) && methodExclude.value().equals(id)) {

                        List<String> fieldProperties = Arrays.stream(methodExclude.properties()).collect(Collectors.toList());

                        if (CollectionUtils.isNotEmpty(fieldProperties)) {
                            Object value = ReflectionUtils.invokeMethod(data, readMethod, new LinkedList<>());
                            returnValue.put(descriptor.getName(), getPropertyValue(id, value, fieldProperties));

                        }

                        continue;
                    }

                }
            }

            returnValue.put(descriptor.getName(), ReflectionUtils.getFieldValue(field, data));
        }

        return returnValue;
    }

    public Object getPropertyValue(String id, Object value, List<String> excludeProperties) {

        if (Objects.isNull(value)) {
            return null;
        }

        if (Collection.class.isAssignableFrom(value.getClass())) {
            Collection<Object> iterable = Casts.cast(value);
            return iterable.stream().map(o -> getPropertyValue(id, o, excludeProperties)).collect(Collectors.toList());
        }

        Object o = filter(id, value);

        if (Map.class.isAssignableFrom(o.getClass())) {
            Map<String, Object> map = Casts.cast(o);

            excludeProperties.forEach(map::remove);
        }

        return o;
    }

}
