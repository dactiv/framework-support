package com.github.dactiv.framework.commons;

/**
 * 缓存配置
 *
 * @author maurice.chen
 */
public class CacheProperties {

    /**
     * 缓存名称
     */
    private String name;

    /**
     * 超时时间
     */
    private TimeProperties expiresTime;

    public CacheProperties() {
    }

    public CacheProperties(String name, TimeProperties expiresTime) {
        this.name = name;
        this.expiresTime = expiresTime;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置缓存名称
     *
     * @param name 缓存名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取超时时间配置
     *
     * @return 超时时间
     */
    public TimeProperties getExpiresTime() {
        return expiresTime;
    }

    /**
     * 设置超时时间配置
     *
     * @param expiresTime 超时时间配置
     */
    public void setExpiresTime(TimeProperties expiresTime) {
        this.expiresTime = expiresTime;
    }
}
