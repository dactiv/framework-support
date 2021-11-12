package com.github.dactiv.framework.commons.id;

import java.io.Serializable;

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
}
