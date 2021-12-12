package com.github.dactiv.framework.commons.enumerate;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 带有值得枚举接口
 *
 * @param <V> 值类型
 */
public interface ValueEnum<V> {

    String METHOD_NAME = "getValue";

    String FIELD_NAME = "value";

    /**
     * 获取值
     *
     * @return 值
     */
    @JsonValue
    V getValue();
}
