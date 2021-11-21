package com.github.dactiv.framework.commons.enumerate;

/**
 * 带有值得枚举接口
 *
 * @param <V> 值类型
 */
public interface ValueEnum<V> {

    String METHOD_NAME = "getValue";

    /**
     * 获取值
     *
     * @return 值
     */
    V getValue();
}
