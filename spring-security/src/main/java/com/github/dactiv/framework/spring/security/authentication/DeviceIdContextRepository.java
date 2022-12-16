package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.config.DeviceIdProperties;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 设备唯一识别的 spring security context 仓库实现,用于移动端通过设备唯一识别获取当前 security context 使用
 *
 * @author maurice
 */
public class DeviceIdContextRepository extends HttpSessionSecurityContextRepository {

    private final AuthenticationProperties properties;

    private final RedissonClient redissonClient;

    private CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    public DeviceIdContextRepository(AuthenticationProperties properties, RedissonClient redissonClient) {
        this.properties = properties;
        this.redissonClient = redissonClient;
    }

    public DeviceIdContextRepository(AuthenticationProperties properties, RedissonClient redissonClient, CipherAlgorithmService cipherAlgorithmService) {
        this.properties = properties;
        this.redissonClient = redissonClient;
        this.cipherAlgorithmService = cipherAlgorithmService;
    }

    public List<AntPathRequestMatcher> getAntPathRequestMatcher() {
        List<AntPathRequestMatcher> antPathRequestMatcher = new LinkedList<>();
        antPathRequestMatcher.add(new AntPathRequestMatcher(this.properties.getLoginProcessingUrl()));
        if (CollectionUtils.isNotEmpty(properties.getPermitUriAntMatchers())) {
            properties.getPermitUriAntMatchers().forEach(AntPathRequestMatcher::new);
        }

        return antPathRequestMatcher;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {

        SecurityContext superSecurityContext = super.loadContext(requestResponseHolder);

        Authentication authentication = superSecurityContext.getAuthentication();
        if (Objects.nonNull(authentication) && isSecurityUserDetails(authentication.getDetails())) {
            return superSecurityContext;
        }

        HttpServletRequest request = requestResponseHolder.getRequest();

        String token = request.getHeader(properties.getDeviceId().getDeviceTokenHeaderName());
        if (StringUtils.isBlank(token)) {
            return superSecurityContext;
        }

        String userId = request.getHeader(properties.getDeviceId().getAccessUserIdHeaderName());
        if (StringUtils.isBlank(userId)) {
            return superSecurityContext;
        }

        String deviceId = this.getDeviceId(token);
        RBucket<SecurityContext> bucket = getSecurityContextBucket(deviceId);
        SecurityContext cacheSecurityContext = bucket.get();

        if (isCurrentUserSecurityContext(userId, cacheSecurityContext, deviceId)) {
            TimeProperties time = properties.getDeviceId().getCache().getExpiresTime();
            if (Objects.nonNull(time)) {
                bucket.expireAsync(time.getValue(), time.getUnit());
            }
            return cacheSecurityContext;
        }

        return superSecurityContext;
    }

    public String getDeviceId(String token) {
        CipherService cipherService = cipherAlgorithmService.getCipherService(properties.getDeviceId().getCipherAlgorithmName());
        ByteSource byteSource = cipherService.decrypt(Base64.decode(token),Base64.decode(properties.getDeviceId().getKey()));
        String text = new String(byteSource.obtainBytes(), StandardCharsets.UTF_8);
        return StringUtils.substringBefore(text, CacheProperties.DEFAULT_SEPARATOR);
    }

    public ByteSource createToken(MobileUserDetails mobileUserDetails) {
        String text = mobileUserDetails.getDeviceIdentified()
                + CacheProperties.DEFAULT_SEPARATOR
                + mobileUserDetails.getUsername()
                + CacheProperties.DEFAULT_SEPARATOR
                + mobileUserDetails.getId()
                + CacheProperties.DEFAULT_SEPARATOR
                + System.currentTimeMillis();
        CipherService cipherService = cipherAlgorithmService.getCipherService(properties.getDeviceId().getCipherAlgorithmName());

        return cipherService.encrypt(text.getBytes(StandardCharsets.UTF_8),Base64.decode(properties.getDeviceId().getKey()));
    }

    public void deleteByMobileUserDetails(MobileUserDetails mobileUserDetails) {
        RBucket<SecurityContext> bucket = getSecurityContextBucket(mobileUserDetails.getDeviceIdentified());
        bucket.deleteAsync();
    }

    public void deleteByDeviceId(String deviceId) {
        RBucket<SecurityContext> bucket = getSecurityContextBucket(deviceId);
        bucket.deleteAsync();
    }

    private boolean isSecurityUserDetails(Object details) {
        return SecurityUserDetails.class.isAssignableFrom(details.getClass());
    }

    private boolean isCurrentUserSecurityContext(String userId, SecurityContext securityContext, String deviceId) {

        if (Objects.nonNull(securityContext) && Objects.nonNull(securityContext.getAuthentication())) {
            Object details = securityContext.getAuthentication().getDetails();

            if (isSecurityUserDetails(details)) {
                SecurityUserDetails userDetails = Casts.cast(details);
                Object id = userDetails.getId();

                return (Objects.nonNull(id) && id.toString().equals(userId)) || deviceId.equals(userId);
            } else {
                return deviceId.equals(userId);
            }
        }

        return false;

    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {

        if (properties.getDeviceId().isOverwriteSession() && isSaveContextToSession(context, request, response)) {
            super.saveContext(context, request, response);
        }

        if (Objects.isNull(context.getAuthentication()) || !context.getAuthentication().isAuthenticated()) {
            return;
        }

        Object details = context.getAuthentication().getDetails();
        if (Objects.isNull(details) || !isSecurityUserDetails(details)) {
            return;
        }

        if (!MobileUserDetails.class.isAssignableFrom(details.getClass())) {
            return ;
        }

        MobileUserDetails mobileUserDetails = Casts.cast(details);
        String token = mobileUserDetails.getDeviceIdentified();

        RBucket<SecurityContext> bucket = getSecurityContextBucket(token);
        SecurityContext cacheSecurityContext = bucket.get();

        if (Objects.nonNull(cacheSecurityContext)) {

            String userId = request.getHeader(properties.getDeviceId().getAccessUserIdHeaderName());

            if (StringUtils.isBlank(userId) && getAntPathRequestMatcher().stream().anyMatch(s -> s.matches(request))) {
                setSecurityContext(context, bucket);
            } else if (isCurrentUserSecurityContext(userId, cacheSecurityContext, token)) {
                setSecurityContext(context, bucket);
            }

        } else {
            setSecurityContext(context, bucket);
        }

    }

    /**
     * 是否将 context 保存到 session 中
     *
     * @param context  安全上下文
     * @param request  http request
     * @param response http response
     *
     * @return true 是，否则 false
     */
    protected boolean isSaveContextToSession(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    /**
     * 设置安全上下文
     *
     * @param context 安全上下文
     * @param bucket  安全上下文的桶对象
     */
    protected void setSecurityContext(SecurityContext context, RBucket<SecurityContext> bucket) {

        TimeProperties time = properties.getDeviceId().getCache().getExpiresTime();

        if (Objects.nonNull(time)) {
            bucket.set(context, time.getValue(), time.getUnit());
        } else {
            bucket.set(context);
        }

        SecurityContextHolder.setContext(context);
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {

        boolean result = false;

        String id = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotBlank(id)) {

            RBucket<SecurityContext> bucket = getSecurityContextBucket(id);
            SecurityContext securityContext = bucket.get();

            String userId = request.getHeader(properties.getDeviceId().getAccessUserIdHeaderName());
            result = isCurrentUserSecurityContext(userId, securityContext, id);
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
     * 获取认证配置信息
     *
     * @return 认证配置信息
     */
    public AuthenticationProperties getProperties() {
        return properties;
    }

    /**
     * 获取 redis 客户端
     *
     * @return redis 客户端
     */
    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    /**
     * 获取加密算法服务
     *
     * @return 加密算法服务
     */
    public CipherAlgorithmService getCipherAlgorithmService() {
        return cipherAlgorithmService;
    }

    /**
     * 设置加密算法服务
     *
     * @param cipherAlgorithmService 加密算法服务
     */
    public void setCipherAlgorithmService(CipherAlgorithmService cipherAlgorithmService) {
        this.cipherAlgorithmService = cipherAlgorithmService;
    }

    /**
     * 创建设备识别认证的头信息
     *
     * @param properties       认证配置信息
     * @param type             认证类型
     * @param deviceIdentified 设备唯一识别
     * @param userId           用户 id
     *
     * @return 头信息
     */
    public static HttpHeaders ofHttpHeaders(AuthenticationProperties properties,
                                            String type,
                                            String deviceIdentified,
                                            Object userId) {

        HttpHeaders httpHeaders = new HttpHeaders();

        if (StringUtils.isNotBlank(deviceIdentified)) {
            httpHeaders.add(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME, deviceIdentified);
        }

        if (Objects.nonNull(properties)) {
            httpHeaders.add(properties.getDeviceId().getAccessUserIdHeaderName(), userId.toString());
        } else {
            httpHeaders.add(DeviceIdProperties.DEFAULT_USER_ID_HEADER_NAME, userId.toString());
        }

        if (StringUtils.isNotBlank(type)) {
            if (Objects.nonNull(properties)) {
                httpHeaders.add(properties.getTypeHeaderName(), type);
            } else {
                httpHeaders.add(AuthenticationProperties.SECURITY_FORM_TYPE_HEADER_NAME, type);
            }
        }

        return httpHeaders;
    }

    /**
     * 创建设备识别认证的头信息
     *
     * @param type             认证类型
     * @param deviceIdentified 设备唯一识别
     * @param userId           用户 id
     *
     * @return 头信息
     */
    public static HttpHeaders ofHttpHeaders(String type, String deviceIdentified, Object userId) {
        return ofHttpHeaders(null, type, deviceIdentified, userId);
    }

    /**
     * 创建带设备识别认证的头信息的 http 实体
     *
     * @param deviceIdentified 设备唯一识别
     * @param userId           用户 id
     *
     * @return 头信息
     */
    public static <T> HttpEntity<T> ofHttpEntity(T body, String deviceIdentified, Object userId) {
        return ofHttpEntity(body, null, deviceIdentified, userId);
    }

    /**
     * 创建带设备识别认证的头信息的 http 实体
     *
     * @param type             认证类型
     * @param deviceIdentified 设备唯一识别
     * @param userId           用户 id
     *
     * @return 头信息
     */
    public static <T> HttpEntity<T> ofHttpEntity(T body, String type, String deviceIdentified, Object userId) {
        return new HttpEntity<>(body, ofHttpHeaders(type, deviceIdentified, userId));
    }

}
