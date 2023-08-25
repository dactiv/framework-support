package com.github.dactiv.framework.idempotent.config;

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

    /**
     * 并发锁 key 前缀
     */
    private String concurrentKeyPrefix = "concurrent:";

    /**
     * 幂等锁 key 前缀
     */
    private String idempotentKeyPrefix = "idempotent:";

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

    /**
     * 获取并发锁 key 前缀
     *
     * @return 并发锁 key 前缀
     */
    public String getConcurrentKeyPrefix() {
        return concurrentKeyPrefix;
    }

    /**
     * 设置并发锁 key 前缀
     *
     * @param concurrentKeyPrefix 并发锁 key 前缀
     */
    public void setConcurrentKeyPrefix(String concurrentKeyPrefix) {
        this.concurrentKeyPrefix = concurrentKeyPrefix;
    }

    /**
     * 获取幂等锁 key 前缀
     *
     * @return 幂等锁 key 前缀
     */
    public String getIdempotentKeyPrefix() {
        return idempotentKeyPrefix;
    }

    /**
     * 设置幂等锁 key 前缀
     *
     * @param idempotentKeyPrefix 幂等锁 key 前缀
     */
    public void setIdempotentKeyPrefix(String idempotentKeyPrefix) {
        this.idempotentKeyPrefix = idempotentKeyPrefix;
    }
}
