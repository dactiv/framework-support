package com.github.dactiv.framework.mybatis.plus.baisc;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.BasicIdentification;

/**
 * 字符串类型的主键，且带逻辑删除标记
 *
 * @author maurice.chen
 */
public interface StringLogicDeleteEntity extends BasicIdentification<String> {

    /**
     * 创建时间字段名称
     */
    String DELETE_FIELD_NAME = "delete";

    /**
     * 获取是否删除该记录
     *
     * @return {@link YesOrNo#Yes} 是，否则 {@link YesOrNo#No}
     */
    YesOrNo getDeleted();

    /**
     * 设置记录是否删除
     *
     * @param deleted {@link YesOrNo#Yes} 是，否则 {@link YesOrNo#No}
     */
    void setDeleted(YesOrNo deleted);
}
