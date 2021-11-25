package com.github.dactiv.framework.commons.id.number;

import com.github.dactiv.framework.commons.id.BasicIdentification;

import java.util.Date;

/**
 * 数字值的主键实体接口
 *
 * @author maurice.chen
 */
public interface NumberIdEntity<T extends Number> extends BasicIdentification<T> {

    /**
     * 创建时间字段名称
     */
    String CREATION_TIME_FIELD_NAME = "creationTime";

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    T getId();

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    Date getCreationTime();

}
