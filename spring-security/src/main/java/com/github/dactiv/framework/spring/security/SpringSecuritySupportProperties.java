package com.github.dactiv.framework.spring.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * spring security 扩展的配置内容
 *
 * @author maurice.chen
 */
@ConfigurationProperties("spring.security.support")
public class SpringSecuritySupportProperties {

    /**
     * 默认存储在 redis 的 security context key 名称
     */
    public final static String DEFAULT_SPRING_SECURITY_CONTEXT_KEY = "spring:security:context:";

    /**
     * 默认登陆处理 url
     */
    public final static String DEFAULT_LOGIN_PROCESSING_URL = "/login";

    /**
     * 安唯一识别存储于在 redis 的当前用户信息 key 前缀
     */
    private String springSecurityContextKey = DEFAULT_SPRING_SECURITY_CONTEXT_KEY;

    /**
     * 登陆 url
     */
    private String loginProcessingUrl = DEFAULT_LOGIN_PROCESSING_URL;

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
     * 获取安唯一识别存储于在 redis 的当前用户信息 key 前缀
     *
     * @return 安唯一识别存储于在 redis 的当前用户信息 key 前缀
     */
    public String getSpringSecurityContextKey() {
        return springSecurityContextKey;
    }

    /**
     * 设置安唯一识别存储于在 redis 的当前用户信息 key 前缀
     *
     * @param springSecurityContextKey 安唯一识别存储于在 redis 的当前用户信息 key 前缀
     */
    public void setSpringSecurityContextKey(String springSecurityContextKey) {
        this.springSecurityContextKey = springSecurityContextKey;
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
