package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.mobile.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 设备唯一识别的 spring security context 仓库实现,用于移动端通过设备唯一识别获取当前 security context 使用
 *
 * @author maurice
 */
public class DeviceIdSecurityContextRepository extends HttpSessionSecurityContextRepository {

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

    private CacheProperties cache = DEFAULT_CACHE;

    private RedissonClient redissonClient;

    private String loginProcessingUrl;

    public DeviceIdSecurityContextRepository() {
    }

    public DeviceIdSecurityContextRepository(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public DeviceIdSecurityContextRepository(CacheProperties cache, RedissonClient redissonClient) {
        this.cache = cache;
        this.redissonClient = redissonClient;
    }

    public DeviceIdSecurityContextRepository(CacheProperties cache, RedissonClient redissonClient, String loginProcessingUrl) {
        this.cache = cache;
        this.redissonClient = redissonClient;
        this.loginProcessingUrl = loginProcessingUrl;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {

        SecurityContext superSecurityContext = super.loadContext(requestResponseHolder);

        HttpServletRequest request = requestResponseHolder.getRequest();

        String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotEmpty(token)) {

            String userId = request.getHeader(DEFAULT_USER_ID_HEADER_NAME);

            if (StringUtils.isNotEmpty(userId)) {

                RBucket<SecurityContext> bucket = getSecurityContextBucket(token);

                SecurityContext cacheSecurityContext = bucket.get();

                if (isCurrentUserSecurityContext(userId, cacheSecurityContext, token)) {
                    return cacheSecurityContext;
                }
            }
        }

        return superSecurityContext;
    }

    private boolean isCurrentUserSecurityContext(String userId, SecurityContext securityContext, String token) {

        if (securityContext != null && securityContext.getAuthentication() != null) {
            Object details = securityContext.getAuthentication().getDetails();

            if (SecurityUserDetails.class.isAssignableFrom(details.getClass())) {

                SecurityUserDetails userDetails = Casts.cast(details);

                Object id = userDetails.getId();

                return (Objects.nonNull(id) && id.toString().equals(userId)) || token.equals(userId);

            } else {
                return token.equals(userId);
            }
        } else {
            return false;
        }

    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {

        super.saveContext(context, request, response);

        String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotEmpty(token)) {

            SaveContextOnUpdateOrErrorResponseWrapper responseWrapper = WebUtils.getNativeResponse(
                    response,
                    SaveContextOnUpdateOrErrorResponseWrapper.class
            );

            if (Objects.nonNull(responseWrapper) && !responseWrapper.isContextSaved() && Objects.nonNull(context.getAuthentication()) && context.getAuthentication().isAuthenticated()) {

                Object details = context.getAuthentication().getDetails();

                if (details != null && SecurityUserDetails.class.isAssignableFrom(details.getClass())) {

                    RBucket<SecurityContext> bucket = getSecurityContextBucket(token);
                    SecurityContext cacheSecurityContext = bucket.get();

                    if (cacheSecurityContext != null) {

                        String userId = request.getHeader(DEFAULT_USER_ID_HEADER_NAME);

                        if (StringUtils.isEmpty(userId) && StringUtils.equals(request.getRequestURI(), loginProcessingUrl)) {
                            bucket.set(context, cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
                            SecurityContextHolder.setContext(context);
                        } else if (isCurrentUserSecurityContext(userId, cacheSecurityContext, token)) {

                            if (MobileUserDetails.class.isAssignableFrom(details.getClass())) {

                                MobileUserDetails mobileUserDetails = Casts.cast(details);

                                if (StringUtils.isNotEmpty(mobileUserDetails.getDeviceIdentified())) {
                                    bucket = getSecurityContextBucket(mobileUserDetails.getDeviceIdentified());
                                }
                            }

                            bucket.set(context, cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
                            SecurityContextHolder.setContext(context);
                        }

                    } else {
                        bucket.set(context, cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
                        SecurityContextHolder.setContext(context);
                    }

                }
            }
        }

    }

    @Override
    public boolean containsContext(HttpServletRequest request) {

        boolean result = false;

        String id = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotEmpty(id)) {

            RBucket<SecurityContext> bucket = getSecurityContextBucket(id);

            SecurityContext securityContext = bucket.get();

            String userId = request.getHeader(DEFAULT_USER_ID_HEADER_NAME);

            result = isCurrentUserSecurityContext(userId, securityContext, id) || securityContext != null;
        }

        if (!result) {
            result = super.containsContext(request);
        }

        return result;
    }

    /**
     * 获取 spring 安全上下文的 桶
     *
     * @param deviceIdentified 设备唯一识别
     *
     * @return redis 桶
     */
    public RBucket<SecurityContext> getSecurityContextBucket(String deviceIdentified) {
        return redissonClient.getBucket(cache.getName() + deviceIdentified);
    }

    /**
     * 创建设备识别认证的头信息
     *
     * @param deviceIdentified 设备唯一识别
     * @param userId 用户 id
     *
     * @return 头信息
     */
    public static HttpHeaders createDeviceIdAuthHeaders(String deviceIdentified, Object userId) {
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME, deviceIdentified);
        httpHeaders.add(DEFAULT_USER_ID_HEADER_NAME, userId.toString());

        return httpHeaders;
    }

}
