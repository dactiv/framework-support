package com.github.dactiv.framework.commons.id;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 基础 id 接口，用于对数据对象操作的统一继承接口。
 *
 * @param <T> id 类型
 */
public interface BasicIdentification<T> extends Serializable {

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    T getId();

    /**
     * 设置主键 id
     * @param id 主键 id
     */
    void setId(T id);

    /**
     * 创建一个新的带 id 值的对象
     *
     * @param <N> 返回类型
     *
     * @return 新的对象
     */
    default <N extends BasicIdentification<T>> N ofNew(){
        N result;

        try {
            result = Casts.cast(this.getClass().getConstructor().newInstance());
        } catch (Exception e) {
            throw new SystemException("对类型为 [" + this.getClass() + "] 的对象创建新实例时出错",e);
        }

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(this.getClass());

        Arrays
                .stream(propertyDescriptors)
                .filter(p -> Objects.nonNull(p.getReadMethod()))
                .filter(p -> Objects.nonNull(p.getWriteMethod()))
                .filter(p -> ClassUtils.isAssignable(p.getWriteMethod().getParameterTypes()[0], p.getReadMethod().getReturnType()))
                .map(PropertyDescriptor::getWriteMethod)
                .forEach(method -> ReflectionUtils.invokeMethod(method, result, new Object[]{null}));

        result.setId(getId());

        return result;
    }
}
