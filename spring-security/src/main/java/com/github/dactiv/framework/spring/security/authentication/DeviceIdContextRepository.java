package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.config.DeviceIdProperties;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.mobile.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpEntity;
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

/**
 * 设备唯一识别的 spring security context 仓库实现,用于移动端通过设备唯一识别获取当前 security context 使用
 *
 * @author maurice
 */
public class DeviceIdContextRepository extends HttpSessionSecurityContextRepository {

    private final AuthenticationProperties properties;

    private final RedissonClient redissonClient;

    public DeviceIdContextRepository(AuthenticationProperties properties, RedissonClient redissonClient) {
        this.properties = properties;
        this.redissonClient = redissonClient;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {

        SecurityContext superSecurityContext = super.loadContext(requestResponseHolder);

        HttpServletRequest request = requestResponseHolder.getRequest();

        String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotEmpty(token)) {

            String userId = request.getHeader(DeviceIdProperties.DEFAULT_SPRING_SECURITY_CONTEXT_KEY);

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
        }

        return false;

    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {

        super.saveContext(context, request, response);

        String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isEmpty(token)) {
            return ;
        }

        SaveContextOnUpdateOrErrorResponseWrapper responseWrapper = WebUtils.getNativeResponse(
                response,
                SaveContextOnUpdateOrErrorResponseWrapper.class
        );

        if (Objects.isNull(responseWrapper) || !responseWrapper.isContextSaved()) {
            return ;
        }

        if (Objects.isNull(context.getAuthentication()) || !context.getAuthentication().isAuthenticated()) {
            return ;
        }

        Object details = context.getAuthentication().getDetails();

        if (Objects.isNull(details) || !SecurityUserDetails.class.isAssignableFrom(details.getClass())) {
            return ;
        }

        RBucket<SecurityContext> bucket = getSecurityContextBucket(token);
        SecurityContext cacheSecurityContext = bucket.get();

        if (Objects.nonNull(cacheSecurityContext)) {

            String userId = request.getHeader(DeviceIdProperties.DEFAULT_USER_ID_HEADER_NAME);

            if (StringUtils.isEmpty(userId) && StringUtils.equals(request.getRequestURI(), properties.getLoginProcessingUrl())) {
                setSecurityContext(context, bucket);
            } else if (isCurrentUserSecurityContext(userId, cacheSecurityContext, token)) {

                if (MobileUserDetails.class.isAssignableFrom(details.getClass())) {

                    MobileUserDetails mobileUserDetails = Casts.cast(details);

                    if (StringUtils.isNotEmpty(mobileUserDetails.getDeviceIdentified())) {
                        bucket = getSecurityContextBucket(mobileUserDetails.getDeviceIdentified());
                    }
                }

                setSecurityContext(context, bucket);
            }

        } else {
            setSecurityContext(context, bucket);
        }

    }

    /**
     * 设置安全上下文
     *
     * @param context 安全上下文
     * @param bucket 安全上下文的桶对象
     */
    private void setSecurityContext(SecurityContext context, RBucket<SecurityContext> bucket) {
        bucket.set(
                context,
                properties.getDeviceId().getCache().getExpiresTime().getValue(),
                properties.getDeviceId().getCache().getExpiresTime().getUnit()
        );
        SecurityContextHolder.setContext(context);
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {

        boolean result = false;

        String id = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotEmpty(id)) {

            RBucket<SecurityContext> bucket = getSecurityContextBucket(id);

            SecurityContext securityContext = bucket.get();

            String userId = request.getHeader(DeviceIdProperties.DEFAULT_USER_ID_HEADER_NAME);

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
        return redissonClient.getBucket(properties.getDeviceId().getCache().getName(deviceIdentified));
    }

    /**
     * 创建设备识别认证的头信息
     *
     * @param deviceIdentified 设备唯一识别
     * @param userId 用户 id
     *
     * @return 头信息
     */
    public static HttpHeaders ofHttpHeaders(String deviceIdentified, Object userId) {
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME, deviceIdentified);
        httpHeaders.add(DeviceIdProperties.DEFAULT_USER_ID_HEADER_NAME, userId.toString());

        return httpHeaders;
    }

    /**
     * 创建带设备识别认证的头信息的 http 实体
     *
     * @param deviceIdentified 设备唯一识别
     * @param userId 用户 id
     *
     * @return 头信息
     */
    public static <T> HttpEntity<T> ofHttpEntity(T body, String deviceIdentified, Object userId) {
        return new HttpEntity<>(body, ofHttpHeaders(deviceIdentified, userId));
    }

}
