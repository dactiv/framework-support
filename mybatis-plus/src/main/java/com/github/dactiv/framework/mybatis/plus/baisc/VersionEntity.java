package com.github.dactiv.framework.mybatis.plus.baisc;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.BasicIdentification;

import java.io.Serializable;

/**
 * 带版本号更新的主键 id 实体
 *
 * @param <V> 版本号类型
 * @param <T> 主键 id 类型
 */
public interface VersionEntity<V, T extends Serializable> extends BasicIdentification<T> {

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
        VersionEntity<V, T> result = BasicIdentification.super.ofNew();
        result.setVersion(getVersion());
        return Casts.cast(result);
    }

    @Override
    default <N extends BasicIdentification<T>> N ofIdData() {
        VersionEntity<V, T> result = BasicIdentification.super.ofIdData();
        result.setVersion(getVersion());
        return Casts.cast(result);
    }
}
