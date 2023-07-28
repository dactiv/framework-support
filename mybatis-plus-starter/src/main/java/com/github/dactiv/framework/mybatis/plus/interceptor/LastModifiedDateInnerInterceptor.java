package com.github.dactiv.framework.mybatis.plus.interceptor;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.mybatis.plus.annotation.LastModifiedDate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 最后更新时间内部拦截器
 *
 * @author maurice.chen
 */
public class LastModifiedDateInnerInterceptor implements InnerInterceptor {
    /**
     * entity类缓存
     */
    private static final Map<String, Class<?>> ENTITY_CLASS_CACHE = new ConcurrentHashMap<>();

    private boolean snakeCase = true;

    private boolean wrapperMode = false;

    public LastModifiedDateInnerInterceptor() {
    }

    public LastModifiedDateInnerInterceptor(boolean wrapperMode) {
        this.wrapperMode = wrapperMode;
    }

    public LastModifiedDateInnerInterceptor(boolean wrapperMode, boolean snakeCase) {
        this.wrapperMode = wrapperMode;
        this.snakeCase = snakeCase;
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        if (SqlCommandType.UPDATE != ms.getSqlCommandType()) {
            return;
        }
        if (parameter instanceof Map) {
            Map<String, Object> map = Casts.cast(parameter);
            doSetLastModifiedDate(map, ms.getId());
        }
    }

    private void doSetLastModifiedDate(Map<String, Object> map, String msId) {
        // updateById(et), update(et, wrapper);
        Object et = map.getOrDefault(Constants.ENTITY, null);
        if (Objects.nonNull(et)) {
            // LastModifiedDate field
            List<Field> fields = this.getLastModifiedDateField(et.getClass());
            if (CollectionUtils.isEmpty(fields)) {
                return;
            }

            fields.forEach(field -> this.setLastModifiedDateValue(et, field));
        }

        // update(LambdaUpdateWrapper) or update(UpdateWrapper)
        else if (wrapperMode && map.entrySet().stream().anyMatch(t -> Objects.equals(t.getKey(), Constants.WRAPPER))) {
            this.setLastModifiedDateByWrapper(map, msId);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setLastModifiedDateByWrapper(Map<String, Object> map, String msId) {
        Object ew = map.get(Constants.WRAPPER);
        if (ew instanceof AbstractWrapper && ew instanceof Update updateWrapper) {
            Class<?> entityClass = ENTITY_CLASS_CACHE.get(msId);
            if (null == entityClass) {
                try {
                    final String className = msId.substring(0, msId.lastIndexOf('.'));
                    entityClass = ReflectionKit.getSuperClassGenericType(Class.forName(className), Mapper.class, 0);
                    ENTITY_CLASS_CACHE.put(msId, entityClass);
                } catch (ClassNotFoundException e) {
                    throw ExceptionUtils.mpe(e);
                }
            }

            List<Field> fields = this.getLastModifiedDateField(entityClass);
            if (CollectionUtils.isEmpty(fields)) {
                return;
            }

            if (snakeCase) {
                fields.forEach(f -> updateWrapper.set(Casts.toSnakeCase(f.getName()), getDateValue(f)));
            } else {
                fields.forEach(f -> updateWrapper.set(f.getName(), getDateValue(f)));
            }
        }
    }

    private void setLastModifiedDateValue(Object target, Field field) {
        Object value = getDateValue(field);
        ReflectionUtils.setFieldValue(target, field.getName(), value);
    }

    public Object getDateValue(Field field) {
        Class<?> fieldType = field.getType();
        if (Date.class.isAssignableFrom(fieldType)) {
            return new Date();
        } else if (LocalDateTime.class.isAssignableFrom(fieldType)) {
            return LocalDateTime.now();
        } else if (LocalDate.class.isAssignableFrom(fieldType)) {
            return LocalDate.now();
        }
        return null;
    }

    private List<Field> getLastModifiedDateField(Class<?> aClass) {
        List<Field> fields = ReflectionUtils.findFields(aClass);
        return fields.stream().filter(f -> Objects.nonNull(AnnotatedElementUtils.findMergedAnnotation(f, LastModifiedDate.class))).collect(Collectors.toList());
    }
}
