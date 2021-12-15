package com.github.dactiv.framework.idempotent;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedList;
import java.util.List;

/**
 * 幂等配置信息
 *
 * @author maurice.chen
 */
@ConfigurationProperties("dactiv.idempotent")
public class IdempotentProperties {

    /**
     * 如果 Idempotent 注解的 value 为空时候，全局忽略的参数类型
     */
    private List<Class<?>> ignoreClasses = new LinkedList<>();

    public IdempotentProperties() {
    }

    /**
     * 获取全局忽略的参数类型
     *
     * @return 全局忽略的参数类型
     */
    public List<Class<?>> getIgnoreClasses() {
        return ignoreClasses;
    }

    /**
     * 设置全局忽略的参数类型
     *
     * @param ignoreClasses 全局忽略的参数类型
     */
    void setIgnoreClasses(List<Class<?>> ignoreClasses) {
        this.ignoreClasses = ignoreClasses;
    }
}
