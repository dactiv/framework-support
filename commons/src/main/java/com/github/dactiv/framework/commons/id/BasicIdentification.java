package com.github.dactiv.framework.commons.id;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 基础 id 接口，用于对数据对象操作的统一继承接口。
 *
 * @param <T> id 类型
 */
public interface BasicIdentification<T> extends Serializable {

    /**
     * 获取 id 方法名称
     */
    String READ_METHOD_NAME = "getId";

    /**
     * 设置 id 方法名称
     */
    String WRITE_METHOD_NAME = "setId";

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
     * 创建一个新的带 id 值的对象，注意：如果字段存在默认值得情况下，创建的实体会附带字段默认值，
     * 如果需要只创建带 id 值其他任何字段值为 null 的情况下，使用{@link #ofIdData(String...)} ()} 创建
     *
     * @param <N> 返回类型
     *
     * @return 新的对象
     */
    default <N extends BasicIdentification<T>> N ofNew(String ...ignoreProperties){
        N result;

        try {
            result = Casts.cast(this.getClass().getConstructor().newInstance());
        } catch (Exception e) {
            throw new SystemException("对类型为 [" + this.getClass() + "] 的对象创建新实例时出错",e);
        }

        result.setId(getId());

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(this.getClass());
        List<String> ignorePropertyList = Arrays.asList(ignoreProperties);

        List<PropertyDescriptor> propertyDescriptorList = Arrays
                .stream(propertyDescriptors)
                .filter(p -> ignorePropertyList.contains(p.getName()))
                .toList();

        for (PropertyDescriptor propertyDescriptor : propertyDescriptorList) {
            if (Objects.nonNull(propertyDescriptor.getWriteMethod()) && Objects.nonNull(propertyDescriptor.getReadMethod())) {
                Object value = ReflectionUtils.invokeMethod(propertyDescriptor.getReadMethod(), this);
                ReflectionUtils.invokeMethod(propertyDescriptor.getWriteMethod(), result, value);
            } else {
                Field field = ReflectionUtils.findField(this.getClass(), propertyDescriptor.getName());
                if (Objects.isNull(field)) {
                    continue;
                }
                Object value = ReflectionUtils.getField(field, this);
                ReflectionUtils.setField(field, result, value);
            }
        }
        return result;
    }

    /**
     * 创建一个新的带 id 值的对象
     *
     * @param <N> 返回类型
     *
     * @return 新的对象
     */
    default <N extends BasicIdentification<T>> N ofIdData(String ...ignoreProperties){
        N result = ofNew(ignoreProperties);

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(this.getClass());
        List<String> ignorePropertyList = Arrays.asList(ignoreProperties);
        Arrays
                .stream(propertyDescriptors)
                .filter(p -> !ignorePropertyList.contains(p.getName()))
                .filter(p -> Objects.nonNull(p.getReadMethod()))
                .filter(p -> Objects.nonNull(p.getWriteMethod()))
                .filter(p -> !p.getReadMethod().getName().equals(READ_METHOD_NAME))
                .filter(p -> !p.getWriteMethod().getName().equals(WRITE_METHOD_NAME))
                .filter(p -> ClassUtils.isAssignable(p.getWriteMethod().getParameterTypes()[0], p.getReadMethod().getReturnType()))
                .map(PropertyDescriptor::getWriteMethod)
                .forEach(method -> ReflectionUtils.invokeMethod(method, result, new Object[]{null}));

        return result;
    }
}
