package com.github.dactiv.framework.commons.generator;

/**
 * id 生成器
 *
 * @author maurice.chen
 */
public interface IdGenerator<T> {

    /**
     * 生成 id
     *
     * @return id 值
     */
    T generateId();
}
