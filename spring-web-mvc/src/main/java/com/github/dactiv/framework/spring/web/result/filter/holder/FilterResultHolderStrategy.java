package com.github.dactiv.framework.spring.web.result.filter.holder;

/**
 *值持有者策略
 *
 * @author maurice.chen
 */
public interface FilterResultHolderStrategy {

    /**
     * 清除值
     */
    void clear();

    /**
     *
     * 获取值
     *
     * @return 值
     */
    String get();

    /**
     * 设置值
     *
     * @param value 值
     */
    void set(String value);
}
