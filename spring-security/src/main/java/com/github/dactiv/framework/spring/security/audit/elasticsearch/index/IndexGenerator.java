package com.github.dactiv.framework.spring.security.audit.elasticsearch.index;

/**
 * 索引生成器
 *
 * @author maurice.chen
 */
public interface IndexGenerator {

    /**
     * 生成索引
     *
     * @param object 对象信息
     *
     * @return 索引值
     */
    String generateIndex(Object object);
}
