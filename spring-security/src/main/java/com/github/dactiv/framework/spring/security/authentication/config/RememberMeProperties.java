package com.github.dactiv.framework.spring.security.authentication.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;

import java.util.concurrent.TimeUnit;

/**
 * 记住我配置
 *
 * @author maurice.chen
 */
public class RememberMeProperties {

    /**
     * 默认记住我的提交参数名和 cookie 名称
     */
    public static final String DEFAULT_PARAM_NAME = "rememberMe";

    /**
     * 默认记住我的响应当前用户的字段名称
     */
    public static final String DEFAULT_USER_DETAILS_NAME = "details";

    /**
     * 提交记住我的参数名称
     */
    private String paramName = DEFAULT_PARAM_NAME;

    /**
     * 缓存信息
     */
    private CacheProperties cache = new CacheProperties(
            "spring:security:remember-me:",
            new TimeProperties(7, TimeUnit.DAYS)
    );

    /**
     * 是否 base64 加密 cookie 值
     */
    private boolean base64Value = true;

    /**
     * 是否永远都记住我（不用传 rememberMe 参数也记住我）
     */
    private boolean always = false;

    /**
     * 缓存配置，用于记录 cookie 存储时间和服务器 redis key 存储时间
     */
    private CookieProperties cookie = new CookieProperties();

    /**
     * 获取记住我参数名
     * @return 记住我参数名
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * 设置记住我参数名
     *
     * @param paramName 记住我参数名
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * 获取缓存配置，用于记录 cookie 存储时间和服务器 redis key 存储时间
     *
     * @return 换粗配置
     */
    public CacheProperties getCache() {
        return cache;
    }

    /**
     * 设置缓存配置，用于记录 cookie 存储时间和服务器 redis key 存储时间
     *
     * @param cache 缓存配置
     */
    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    /**
     * 判断 cookie 值是否使用 base46 编码后在进行存储
     *
     * @return true 是，否则 false
     */
    public boolean isBase64Value() {
        return base64Value;
    }

    /**
     * 设置 cookie 值是否使用 base46 编码后在进行存储
     * @param base64Value true 是，否则 false
     */
    public void setBase64Value(boolean base64Value) {
        this.base64Value = base64Value;
    }

    /**
     * 判断是否永远使用记住我（不用传 rememberMe 参数也记住我）
     *
     * @return true 是，否则 false
     */
    public boolean isAlways() {
        return always;
    }

    /**
     * 设置是否永远使用记住我（不用传 rememberMe 参数也记住我）
     *
     * @param always true 是，否则 false
     */
    public void setAlways(boolean always) {
        this.always = always;
    }

    /**
     * 获取 cookie 配置
     *
     * @return cookie 配置
     */
    public CookieProperties getCookie() {
        return cookie;
    }

    /**
     * 设置 cookie 配置
     *
     * @param cookie cookie 配置
     */
    public void setCookie(CookieProperties cookie) {
        this.cookie = cookie;
    }

    /**
     * cookie 配置
     *
     * @author maurice.chen
     */
    public static class CookieProperties {

        /**
         * cookie 名称
         */
        private String name = DEFAULT_PARAM_NAME;

        /**
         * 域名
         */
        private String domain;

        /**
         * 是否 https
         */
        private Boolean secure;

        /**
         * 获取名称
         *
         * @return 名称
         */
        public String getName() {
            return name;
        }

        /**
         * 设置名称
         *
         * @param name 名称
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取域名
         *
         * @return 域名
         */
        public String getDomain() {
            return domain;
        }

        /**
         * 设置域名
         *
         * @param domain 域名
         */
        public void setDomain(String domain) {
            this.domain = domain;
        }

        /**
         * 获取是否 https
         *
         * @return secure true 是，否则 false
         */
        public Boolean getSecure() {
            return secure;
        }

        /**
         * 设置是否 https
         *
         * @param secure true 是，否则 false
         */
        public void setSecure(Boolean secure) {
            this.secure = secure;
        }
    }

}
