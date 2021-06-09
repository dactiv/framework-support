package com.github.dactiv.framework.spring.web.result.filter.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.result.filter.FilterPropertyExecutor;
import com.github.dactiv.framework.spring.web.result.filter.annotation.ExcludeProperties;
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

                ExcludeProperties fieldFilter = AnnotationUtils.findAnnotation(field, ExcludeProperties.class);

                if (Objects.nonNull(fieldFilter) && fieldFilter.value().equals(id)) {

                    Object value = returnValue.get(descriptor.getName());

                    List<String> filterProperties = Arrays.stream(fieldFilter.properties()).collect(Collectors.toList());

                    if (fieldFilter.filterClassType()) {

                        ExcludeProperties classExcludeProperties = AnnotationUtils.findAnnotation(field.getType(), ExcludeProperties.class);

                        if (Objects.nonNull(classExcludeProperties) && classExcludeProperties.value().equals(id)) {
                            filterProperties.addAll(Arrays.stream(classExcludeProperties.properties()).collect(Collectors.toList()));
                        }
                    }

                    Object valueResult = filter(value, filterProperties);

                    returnValue.put(field.getName(), valueResult);
                }

            }

            if (Objects.nonNull(field)) {
                ExcludeProperties.Exclude exclude = AnnotationUtils.findAnnotation(field, ExcludeProperties.Exclude.class);

                if (Objects.nonNull(exclude) && exclude.value().equals(id)) {
                    returnValue.remove(field.getName());
                }
            }

            Method readMethod = descriptor.getReadMethod();

            if (Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                ExcludeProperties methodFilter = AnnotationUtils.findAnnotation(readMethod, ExcludeProperties.class);

                if (Objects.nonNull(methodFilter)) {
                    Object value = returnValue.get(descriptor.getName());

                    List<String> filterProperties = Arrays.stream(methodFilter.properties()).collect(Collectors.toList());

                    if (methodFilter.filterClassType()) {

                        ExcludeProperties classExcludeProperties = AnnotationUtils.findAnnotation(readMethod.getReturnType(), ExcludeProperties.class);

                        if (Objects.nonNull(classExcludeProperties) && classExcludeProperties.value().equals(id)) {
                            filterProperties.addAll(Arrays.stream(classExcludeProperties.properties()).collect(Collectors.toList()));
                        }
                    }

                    Object valueResult = filter(value, filterProperties);

                    returnValue.put(descriptor.getName(), valueResult);
                }

                ExcludeProperties.Exclude exclude = AnnotationUtils.findAnnotation(readMethod, ExcludeProperties.Exclude.class);

                if (Objects.nonNull(exclude) && exclude.value().equals(id)) {
                    returnValue.remove(descriptor.getName());
                }
            }

        }

        for (String property : excludeProperties.properties()) {
            returnValue.remove(property);
        }

        return returnValue;
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
