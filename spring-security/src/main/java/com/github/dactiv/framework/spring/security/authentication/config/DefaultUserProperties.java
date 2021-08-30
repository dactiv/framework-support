package com.github.dactiv.framework.spring.security.authentication.config;

import com.github.dactiv.framework.commons.CacheProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties;

import java.util.List;

/**
 * 默认用户配置
 *
 * @author maurice.chen
 */
public class DefaultUserProperties {

    /**
     * 用户数据
     */
    private List<SecurityProperties.User> data;

    /**
     * 缓存配置
     */
    private CacheProperties cache;

    public DefaultUserProperties() {
    }

    /**
     * 获取用户数据
     *
     * @return 用户数据
     */
    public List<SecurityProperties.User> getData() {
        return data;
    }

    /**
     * 设置用户数据
     *
     * @param data 用户数据
     */
    public void setData(List<SecurityProperties.User> data) {
        this.data = data;
    }

    /**
     * 获取缓存信息
     *
     * @return 缓存信息
     */
    public CacheProperties getCache() {
        return cache;
    }

    /**
     * 设置缓存信息
     *
     * @param cache 缓存信息
     */
    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }
}
