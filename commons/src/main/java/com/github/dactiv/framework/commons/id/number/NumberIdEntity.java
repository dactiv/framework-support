package com.github.dactiv.framework.commons.id.number;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * 数字值的主键实体接口
 *
 * @author maurice.chen
 */
public interface NumberIdEntity<T extends Number> {

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    T getId();

    /**
     * 设置创建时间
     *
     * @return 创建时间
     */
    @JsonIgnore
    Date getCreationTime();

}
