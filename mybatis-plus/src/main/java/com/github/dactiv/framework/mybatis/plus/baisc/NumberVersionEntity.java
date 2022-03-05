package com.github.dactiv.framework.mybatis.plus.baisc;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;

/**
 * 带版本号的实体基类接口
 *
 * @param <V> 版本类型
 * @param <T> 主键类型
 *
 * @author maurice.chen
 */
public interface NumberVersionEntity<V, T extends Number> extends NumberIdEntity<T> {

    /**
     * 创建时间字段名称
     */
    String VERSION_FIELD_NAME = "version";

    /**
     * 设置版本号
     *
     * @param version 版本号
     */
    void setVersion(V version);

    /**
     * 获取版本号
     *
     * @return 版本号
     */
    V getVersion();

    @Override
    default <N extends BasicIdentification<T>> N ofNew() {
        NumberVersionEntity<V, T> result = NumberIdEntity.super.ofNew();
        result.setVersion(getVersion());
        return Casts.cast(result);
    }

    @Override
    default <N extends BasicIdentification<T>> N ofIdData() {
        NumberVersionEntity<V, T> result = NumberIdEntity.super.ofIdData();
        result.setVersion(getVersion());
        return Casts.cast(result);
    }
}
