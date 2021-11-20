package com.github.dactiv.framework.commons.id;

import com.github.dactiv.framework.commons.Casts;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

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
        N result = null;

        try {
            result = Casts.cast(this.getClass().getConstructor().newInstance());
            result.setId(getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
