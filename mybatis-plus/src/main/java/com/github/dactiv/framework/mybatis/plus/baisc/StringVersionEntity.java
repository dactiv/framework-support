package com.github.dactiv.framework.mybatis.plus.baisc;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.BasicIdentification;

/**
 * 字符串类类型的主键且带版本号的实体基类接口
 *
 * @param <V> 版本号类型
 *
 * @author maurice.chen
 */
public interface StringVersionEntity<V> extends BasicIdentification<String> {

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
    default <N extends BasicIdentification<String>> N ofNew() {
        StringVersionEntity<V> result = BasicIdentification.super.ofNew();
        result.setVersion(getVersion());
        return Casts.cast(result);
    }

    @Override
    default <N extends BasicIdentification<String>> N ofIdData() {
        StringVersionEntity<V> result = BasicIdentification.super.ofIdData();
        result.setVersion(getVersion());
        return Casts.cast(result);
    }
}
