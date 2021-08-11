package com.github.dactiv.framework.commons.id;

import java.io.Serializable;

/**
 * 主键实体
 *
 * @param <T> 主键类型
 */
public class IdEntity<T> implements Serializable {

    /**
     * id 字段名称
     */
    public static final String ID_FIELD_NAME = "id";

    /**
     * 主键 id
     */
    private T id;

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    public T getId() {
        return id;
    }

    /**
     * 设置主键 id
     *
     * @param id 主键 id
     */
    public void setId(T id) {
        this.id = id;
    }
}
