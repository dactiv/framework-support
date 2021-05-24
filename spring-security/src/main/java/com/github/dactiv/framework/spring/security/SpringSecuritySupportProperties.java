package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdSecurityContextRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

/**
 * spring security 扩展的配置内容
 *
 * @author maurice.chen
 */
@ConfigurationProperties("spring.security.support")
public class SpringSecuritySupportProperties {

    /**
     * 安唯一识别存储于在 redis 的当前用户信息 key 前缀
     */
    private CacheProperties cache = DeviceIdSecurityContextRepository.DEFAULT_CACHE;

    /**
     * 登陆 url
     */
    private String loginProcessingUrl = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;

    /**
     * 是否允许 session 创建，默认为 true
     */
    private boolean allowSessionCreation = true;

    /**
     * 是否禁用 url 重写，默认为 false
     */
    private boolean disableUrlRewriting = false;

    public SpringSecuritySupportProperties() {
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
     * 获取登陆处理 url
     *
     * @return 登陆处理 url
     */
    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    /**
     * 设置登陆处理 url
     *
     * @param loginProcessingUrl 登陆处理 url
     */
    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }

    /**
     * 获取是否允许 session 创建
     *
     * @return true 是，否则 false
     */
    public boolean getAllowSessionCreation() {
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
     * 获取是否禁用 url 重写
     *
     * @return true 是，否则 false
     */
    public boolean getDisableUrlRewriting() {
        return disableUrlRewriting;
    }

    /**
     * 设置是否禁用 url 重写
     *
     * @param disableUrlRewriting 是否禁用 url 重写
     */
    public void setDisableUrlRewriting(boolean disableUrlRewriting) {
        this.disableUrlRewriting = disableUrlRewriting;
    }
}
