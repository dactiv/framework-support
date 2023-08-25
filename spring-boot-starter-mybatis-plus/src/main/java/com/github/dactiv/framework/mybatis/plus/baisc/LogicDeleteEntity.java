package com.github.dactiv.framework.mybatis.plus.baisc;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 数值型的逻辑删除实体基类接口
 *
 * @param <T> 主键类型
 *
 * @author maurice.chen
 */
public interface LogicDeleteEntity<T> extends BasicIdentification<T> {
    /**
     * 创建时间字段名称
     */
    String DELETE_FIELD_NAME = "deleted";

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

    @Override
    default <N extends BasicIdentification<T>> N ofNew(String ... ignoreProperties) {
        LogicDeleteEntity<T> result = BasicIdentification.super.ofNew(ignoreProperties);
        result.setDeleted(getDeleted());
        return Casts.cast(result);
    }

    @Override
    default <N extends BasicIdentification<T>> N ofIdData(String ... ignoreProperties) {
        List<String> ignorePropertyList = new LinkedList<>();

        CollectionUtils.addAll(ignorePropertyList, ignoreProperties);
        ignorePropertyList.add(DELETE_FIELD_NAME);

        LogicDeleteEntity<T> result = BasicIdentification.super.ofIdData(ignorePropertyList.toArray(new String[0]));
        result.setDeleted(getDeleted());
        return Casts.cast(result);
    }
}
