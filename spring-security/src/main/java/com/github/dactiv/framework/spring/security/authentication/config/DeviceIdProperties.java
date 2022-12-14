package com.github.dactiv.framework.spring.security.authentication.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import org.springframework.security.core.context.SecurityContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public final static String DEFAULT_DEVICE_TOKEN_HEADER_NAME = "X-DEVICE-TOKEN";

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
     * 唯一识别 token 头名称
     */
    private String deviceTokenHeaderName = DEFAULT_DEVICE_TOKEN_HEADER_NAME;

    /**
     * 如果 {@link org.springframework.security.core.context.SecurityContext} 在 session
     * 找不到但通过设备唯一能找到时，是否将该值通过
     * {@link org.springframework.security.web.context.HttpSessionSecurityContextRepository#saveContext(SecurityContext, HttpServletRequest, HttpServletResponse)}
     * 保存到 session 中
     */
    private boolean overwriteSession = true;

    /**
     * 加解密算法名称
     */
    private String cipherAlgorithmName = CipherAlgorithmService.AES_ALGORITHM;

    /**
     * 加解密密钥
     */
    private String key;

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
     *
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

    /**
     * 是否覆盖 session
     * <p>
     * 说明: 如果 {@link org.springframework.security.core.context.SecurityContext} 在 session
     * 找不到但通过设备唯一能找到时，是否将该值通过
     * {@link org.springframework.security.web.context.HttpSessionSecurityContextRepository#saveContext(SecurityContext, HttpServletRequest, HttpServletResponse)}
     * 保存到 session 中
     * </p>
     *
     * @return true 是，否则 false
     */
    public boolean isOverwriteSession() {
        return overwriteSession;
    }

    /**
     * 设置是否覆盖 session
     *
     * <p>
     * 说明: 如果 {@link org.springframework.security.core.context.SecurityContext} 在 session
     * 找不到但通过设备唯一能找到时，是否将该值通过
     * {@link org.springframework.security.web.context.HttpSessionSecurityContextRepository#saveContext(SecurityContext, HttpServletRequest, HttpServletResponse)}
     * 保存到 session 中
     * </p>
     *
     * @param overwriteSession true 是，否则 false
     */
    public void setOverwriteSession(boolean overwriteSession) {
        this.overwriteSession = overwriteSession;
    }

    /**
     * 获取加解密算法名称
     *
     * @return 加解密算法名称
     */
    public String getCipherAlgorithmName() {
        return cipherAlgorithmName;
    }

    /**
     * 设置加解密算法名称
     *
     * @param cipherAlgorithmName 加解密算法名称
     */
    public void setCipherAlgorithmName(String cipherAlgorithmName) {
        this.cipherAlgorithmName = cipherAlgorithmName;
    }

    /**
     * 获取加解密密钥
     *
     * @return 加解密密钥
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置加解密密钥
     *
     * @param key 加解密密钥
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 设置唯一识别 token 头名称
     *
     * @return 唯一识别 token 头名称
     */
    public String getDeviceTokenHeaderName() {
        return deviceTokenHeaderName;
    }

    /**
     * 获取唯一识别 token 头名称
     *
     * @param deviceTokenHeaderName 唯一识别 token 头名称
     */
    public void setDeviceTokenHeaderName(String deviceTokenHeaderName) {
        this.deviceTokenHeaderName = deviceTokenHeaderName;
    }
}
