package com.github.dactiv.framework.spring.security.authentication.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * 认证配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("dactiv.authentication")
public class AuthenticationProperties {

    /**
     * 默认的认证类型 header 名称
     */
    public static final String SECURITY_FORM_TYPE_HEADER_NAME = "X-AUTHENTICATION-TYPE";
    /**
     * 默认的认证 token header 名称
     */
    public static final String SECURITY_FORM_TOKEN_HEADER_NAME = "X-AUTHENTICATION-TOKEN";
    /**
     * 默认的认证 token 解析 header 名称
     */
    public static final String SECURITY_FORM_TOKEN_RESOLVER_HEADER_NAME = "X-AUTHENTICATION-TOKEN-RESOLVER";
    /**
     * 默认的认证类型参数名称
     */
    public static final String SECURITY_FORM_TYPE_PARAM_NAME = "authenticationType";
    /**
     * 默认的登陆账户参数名
     */
    public static final String SECURITY_FORM_USERNAME_PARAM_NAME = "username";
    /**
     * 默认的登陆密码参数名
     */
    public static final String SECURITY_FORM_PASSWORD_PARAM_NAME = "password";

    /**
     * 方法的接口路径
     */
    private List<String> permitUriAntMatchers = new LinkedList<>();

    /**
     * 默认用户配置
     */
    private List<SecurityProperties.User> users = new LinkedList<>();

    /**
     * 登陆 url
     */
    private String loginProcessingUrl = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;

    /**
     * 访问 token 配置
     */
    private AccessTokenProperties accessToken = new AccessTokenProperties();

    /**
     * 认证类型 header 名称
     */
    private String typeHeaderName = SECURITY_FORM_TYPE_HEADER_NAME;

    /**
     * 认证 token 名称
     */
    private String tokenHeaderName = SECURITY_FORM_TOKEN_HEADER_NAME;

    /**
     * 认证 token 解析名称
     */
    private String tokenResolverHeaderName = SECURITY_FORM_TOKEN_RESOLVER_HEADER_NAME;

    /**
     * 认证类型参数名称
     */
    private String typeParamName = SECURITY_FORM_TYPE_PARAM_NAME;

    /**
     * 登陆账户参数名
     */
    private String usernameParamName = SECURITY_FORM_USERNAME_PARAM_NAME;

    /**
     * 登陆密码参数名
     */
    private String passwordParamName = SECURITY_FORM_PASSWORD_PARAM_NAME;

    /**
     * 记住我配置
     */
    private RememberMeProperties rememberMe = new RememberMeProperties();

    /**
     * ip 认证配置
     */
    private List<IpAuthenticationProperties> ipAuthentication = new LinkedList<>();

    /**
     * 认证缓存配置信息
     */
    private CacheProperties authenticationCache = CacheProperties.of(RequestAuthenticationProvider.DEFAULT_AUTHENTICATION_KEY_NAME, TimeProperties.ofDay(7));

    /**
     * 授权缓存配置信息
     */
    private CacheProperties authorizationCache = CacheProperties.of(RequestAuthenticationProvider.DEFAULT_AUTHORIZATION_KEY_NAME, TimeProperties.ofDay(7));

    /**
     * 获取默认用户信息集合
     *
     * @return 默认用户信息集合
     */
    public List<SecurityProperties.User> getUsers() {
        return users;
    }

    /**
     * 设置默认用户信息集合
     *
     * @param users 默认用户信息集合
     */
    public void setUsers(List<SecurityProperties.User> users) {
        this.users = users;
    }

    /**
     * 获取处理登陆请求的 url
     *
     * @return 处理登陆请求的 url
     */
    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    /**
     * 设置处理登陆请求的 url
     *
     * @param loginProcessingUrl 处理登陆请求的 url
     */
    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }

    /**
     * 获取访问 token 配置
     *
     * @return 访问 token 配置
     */
    public AccessTokenProperties getAccessToken() {
        return accessToken;
    }

    /**
     * 设置访问 token 配置
     *
     * @param accessToken 访问 token 配置
     */
    public void setAccessToken(AccessTokenProperties accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * 获取认证类型 header 名称
     *
     * @return 认证类型 header 名称
     */
    public String getTypeHeaderName() {
        return typeHeaderName;
    }

    /**
     * 设置认证类型 header 名称
     *
     * @param typeHeaderName 认证类型 header 名称
     */
    public void setTypeHeaderName(String typeHeaderName) {
        this.typeHeaderName = typeHeaderName;
    }

    /**
     * 获取认证类型参数名称
     *
     * @return 认证类型参数名称
     */
    public String getTypeParamName() {
        return typeParamName;
    }

    /**
     * 设置认证类型参数名称
     *
     * @param typeParamName 认证类型参数名称
     */
    public void setTypeParamName(String typeParamName) {
        this.typeParamName = typeParamName;
    }

    /**
     * 获取认证 token 的头名称
     *
     * @return token 的头名称
     */
    public String getTokenHeaderName() {
        return tokenHeaderName;
    }

    /**
     * 设置 token 的头名称
     *
     * @param tokenHeaderName token 的头名称
     */
    public void setTokenHeaderName(String tokenHeaderName) {
        this.tokenHeaderName = tokenHeaderName;
    }

    /**
     * 获取认证 token 解析名称
     *
     * @return 认证 token 解析名称
     */
    public String getTokenResolverHeaderName() {
        return tokenResolverHeaderName;
    }

    /**
     * 设置认证 token 解析名称
     *
     * @param tokenResolverHeaderName 认证 token 解析名称
     */
    public void setTokenResolverHeaderName(String tokenResolverHeaderName) {
        this.tokenResolverHeaderName = tokenResolverHeaderName;
    }

    /**
     * 获取 ip 认证配置
     *
     * @return  ip 认证配置
     */
    public List<IpAuthenticationProperties> getIpAuthentication() {
        return ipAuthentication;
    }

    /**
     * 设置 ip 认证配置
     *
     * @param ipAuthentication  ip 认证配置
     */
    public void setIpAuthentication(List<IpAuthenticationProperties> ipAuthentication) {
        this.ipAuthentication = ipAuthentication;
    }

    /**
     * 获取登陆账户参数名
     *
     * @return 登陆账户参数名
     */
    public String getUsernameParamName() {
        return usernameParamName;
    }

    /**
     * 设置登陆账户参数名
     *
     * @param usernameParamName 登陆账户参数名
     */
    public void setUsernameParamName(String usernameParamName) {
        this.usernameParamName = usernameParamName;
    }

    /**
     * 获取登陆密码参数名
     *
     * @return 登陆密码参数名
     */
    public String getPasswordParamName() {
        return passwordParamName;
    }

    /**
     * 设置登陆密码参数名
     *
     * @param passwordParamName 登陆密码参数名
     */
    public void setPasswordParamName(String passwordParamName) {
        this.passwordParamName = passwordParamName;
    }

    /**
     * 获取开放的接口路径
     *
     * @return 开放的接口路径
     */
    public List<String> getPermitUriAntMatchers() {
        return permitUriAntMatchers;
    }

    /**
     * 设置开放的接口路径
     *
     * @param permitUriAntMatchers 开放的接口路径
     */
    public void setPermitUriAntMatchers(List<String> permitUriAntMatchers) {
        this.permitUriAntMatchers = permitUriAntMatchers;
    }

    /**
     * 获取记住我配置
     *
     * @return 记住我配置
     */
    public RememberMeProperties getRememberMe() {
        return rememberMe;
    }

    /**
     * 设置记住我配置
     *
     * @param rememberMe 记住我配置
     */
    public void setRememberMe(RememberMeProperties rememberMe) {
        this.rememberMe = rememberMe;
    }

    /**
     * 获取认证缓存配置信息
     *
     * @return 认证缓存配置信息
     */
    public CacheProperties getAuthenticationCache() {
        return authenticationCache;
    }

    /**
     * 设置认证缓存配置信息
     *
     * @param authenticationCache 认证缓存配置信息
     */
    public void setAuthenticationCache(CacheProperties authenticationCache) {
        this.authenticationCache = authenticationCache;
    }

    /**
     * 获取授权缓存配置信息
     *
     * @return 授权缓存配置信息
     */
    public CacheProperties getAuthorizationCache() {
        return authorizationCache;
    }

    /**
     * 设置授权缓存配置信息
     *
     * @param authorizationCache 授权缓存配置信息
     */
    public void setAuthorizationCache(CacheProperties authorizationCache) {
        this.authorizationCache = authorizationCache;
    }
}
