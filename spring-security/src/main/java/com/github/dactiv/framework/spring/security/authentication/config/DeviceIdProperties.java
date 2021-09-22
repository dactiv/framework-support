package com.github.dactiv.framework.spring.security.authentication.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;

import java.util.concurrent.TimeUnit;

/**
 * 设备唯一识别配置
 *
 * @author maurice.chen
 */
public class DeviceIdProperties {

    /**
     * 默认的用户 id 头名称
     */
    public final static String DEFAULT_USER_ID_HEADER_NAME = "X-ACCESS-USER-ID";

    /**
     * 默认存储在 redis 的 security context key 名称
     */
    public final static String DEFAULT_SPRING_SECURITY_CONTEXT_KEY = "spring:security:context:";

    /**
     * 默认存储在 redis 的 security context key 缓存配置
     */
    public final static CacheProperties DEFAULT_CACHE = new CacheProperties(
            DEFAULT_SPRING_SECURITY_CONTEXT_KEY,
            new TimeProperties(2592000, TimeUnit.SECONDS)
    );

    /**
     * 是否允许 session 创建，默认为 true
     */
    private boolean allowSessionCreation = true;

    /**
     * 是否禁用 url 重写，默认为 false
     */
    private boolean disableUrlRewriting = false;

    /**
     * 缓存配置
     */
    private CacheProperties cache = DEFAULT_CACHE;

    /**
     * 访问用户 id 头名称
     */
    private String accessUserIdHeaderName = DEFAULT_USER_ID_HEADER_NAME;

    /**
     * 是否允许 session 创建
     *
     * @return true 是，否则 false
     */
    public boolean isAllowSessionCreation() {
        return allowSessionCreation;
    }

    /**
     * 设置是否允许 session 创建
     *
     * @param allowSessionCreation true 是，否则 false
     */
    public void setAllowSessionCreation(boolean allowSessionCreation) {
        this.allowSessionCreation = allowSessionCreation;
    }

    /**
     * 是否禁用 url 重写，默认为 false
     *
     * @return true 是，否则 false
     */
    public boolean isDisableUrlRewriting() {
        return disableUrlRewriting;
    }

    /**
     * 设置是否禁用 url 重写，默认为 false
     * @param disableUrlRewriting true 是，否则 false
     */
    public void setDisableUrlRewriting(boolean disableUrlRewriting) {
        this.disableUrlRewriting = disableUrlRewriting;
    }

    /**
     * 获取缓存配置
     *
     * @return 缓存配置
     */
    public CacheProperties getCache() {
        return cache;
    }

    /**
     * 设置缓存配置
     *
     * @param cache 缓存配置
     */
    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    /**
     * 获取访问用户 id 头名称
     *
     * @return 访问用户 id 头名称
     */
    public String getAccessUserIdHeaderName() {
        return accessUserIdHeaderName;
    }

    /**
     * 设置访问用户 id 头名称
     *
     * @param accessUserIdHeaderName 访问用户 id 头名称
     */
    public void setAccessUserIdHeaderName(String accessUserIdHeaderName) {
        this.accessUserIdHeaderName = accessUserIdHeaderName;
    }
}
