package com.github.dactiv.framework.commons;

import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存配置
 *
 * @author maurice.chen
 */
public class CacheProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 3266643181504654737L;

    /**
     * 默认缓存分隔符
     */
    public static final String DEFAULT_SEPARATOR = ":";

    /**
     * 缓存名称
     */
    private String name;

    /**
     * 超时时间
     */
    private TimeProperties expiresTime;

    /**
     * 缓存配置
     */
    public CacheProperties() {

    }

    /**
     * 缓存配置
     *
     * @param name 缓存名称
     */
    public CacheProperties(String name) {
        this(name, null);
    }

    /**
     * 缓存配置
     *
     * @param name        缓存名称
     * @param expiresTime 超时时间配置
     */
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

    /**
     * 获取缓存名称
     *
     * @param suffix 后缀值
     *
     * @return 缓存名称 + 后缀值，如果缓存名称没有 ":" 符号，会自动加上。
     */
    public String getName(Object suffix) {
        return StringUtils.appendIfMissing(getName(), DEFAULT_SEPARATOR) + suffix.toString();
    }

    /**
     * 创建缓存配置
     *
     * @param name 缓存名称
     *
     * @return 缓存配置
     */
    public static CacheProperties of(String name) {
        return new CacheProperties(name);
    }

    /**
     * 创建缓存配置
     *
     * @param name        缓存配置
     * @param expiresTime 超时时间配置
     *
     * @return 缓存配置
     */
    public static CacheProperties of(String name, TimeProperties expiresTime) {
        return new CacheProperties(name, expiresTime);
    }
}
