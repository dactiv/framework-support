package com.github.dactiv.framework.spring.web.result.filter.holder;

import java.util.List;

/**
 * 值持有者策略
 *
 * @author maurice.chen
 */
public interface FilterResultHolderStrategy {

    /**
     * 清除值
     */
    void clear();

    /**
     * 获取值
     *
     * @return 值集合
     */
    List<String> get();

    /**
     * 设置值
     *
     * @param values 值
     */
    void set(List<String> values);

    /**
     * 添加值
     *
     * @param value 值
     */
    void add(String value);
}
