package com.github.dactiv.framework.spring.security.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.framework.crypto.algorithm.exception.CryptoException;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.spring.security.authentication.config.AccessTokenProperties;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.RememberMeAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.SimpleAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 访问 token 上下文仓库实现，用于移动端用户明细登陆系统后，返回一个 token， 为无状态的 http 传输中，通过该 token 来完成认证授权等所有工作
 *
 * @author maurice.chen
 */
public class AccessTokenContextRepository extends HttpSessionSecurityContextRepository {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(AccessTokenContextRepository.class);

    private final RedissonClient redissonClient;

    //private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private final AuthenticationProperties authenticationProperties;

    private final AntPathRequestMatcher loginRequestMatcher;

    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    public AccessTokenContextRepository(RedissonClient redissonClient, AuthenticationProperties authenticationProperties) {
        this.redissonClient = redissonClient;
        this.authenticationProperties = authenticationProperties;
        this.loginRequestMatcher = new AntPathRequestMatcher(authenticationProperties.getLoginProcessingUrl(), HttpMethod.POST.name());
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        SecurityContext securityContext = readSecurityContextFromRequest(request);

        if (Objects.isNull(securityContext)) {
            securityContext = super.loadContext(requestResponseHolder);
        }

        return securityContext;
    }

    /*@Override
    public DeferredSecurityContext loadDeferredContext(HttpServletRequest request) {
        DeferredSecurityContext superDeferredSecurityContext = super.loadDeferredContext(request);
        Supplier<SecurityContext> supplier = () -> readSecurityContextFromRequest(request);
        return new AccessTokenDeferredSecurityContext(List.of(superDeferredSecurityContext, supplier), securityContextHolderStrategy);
    }*/

    private SecurityContext readSecurityContextFromRequest(HttpServletRequest request) {
        if (this.loginRequestMatcher.matches(request)) {
            return null;
        }
        String token = request.getHeader(authenticationProperties.getAccessToken().getAccessTokenHeaderName());
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter(authenticationProperties.getAccessToken().getAccessTokenParamName());
        }

        if (StringUtils.isEmpty(token)) {
            return null;
        }

        return getSecurityContext(token);
    }

    public SecurityContext getSecurityContext(String token) {
        CipherService cipherService = cipherAlgorithmService.getCipherService(authenticationProperties.getAccessToken().getCipherAlgorithmName());
        byte[] key = Base64.decode(authenticationProperties.getAccessToken().getKey());

        try {
            ByteSource byteSource = cipherService.decrypt(Base64.decode(token), key);
            String plaintext = new String(byteSource.obtainBytes(), Charset.defaultCharset());

            Map<String, Object> plaintextUserDetail = convertPlaintext(plaintext);
            String type = plaintextUserDetail.get(PluginAuditEvent.TYPE_FIELD_NAME).toString();
            Object id = plaintextUserDetail.get(IdEntity.ID_FIELD_NAME).toString();

            RBucket<SecurityContext> bucket = getSecurityContextBucket(type, id);
            SecurityContext context = bucket.get();
            if (Objects.isNull(context)) {
                return null;
            }

            SecurityUserDetails userDetails = Casts.cast(context.getAuthentication().getDetails());
            String existToken = userDetails.getMeta().getOrDefault(AccessTokenProperties.DEFAULT_ACCESS_TOKEN_PARAM_NAME, StringUtils.EMPTY).toString();

            if (!StringUtils.equals(existToken, token)) {
                return null;
            }

            if (userDetails instanceof MobileUserDetails) {
                MobileUserDetails mobileUserDetails = Casts.cast(userDetails);
                Object deviceIdentified = plaintextUserDetail.get(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME);
                if (!Objects.equals(deviceIdentified, mobileUserDetails.getDeviceIdentified())) {
                    return null;
                }
            }

            return context;

        } catch (CryptoException e) {
            LOGGER.error("通过密钥:" + authenticationProperties.getAccessToken().getKey() + "解密token:" + token + "失败", e);
        }

        return null;
    }

    public RBucket<SecurityContext> getSecurityContextBucket(String type, Object deviceIdentified) {
        String key = authenticationProperties.getAccessToken().getCache().getName(type + CacheProperties.DEFAULT_SEPARATOR + deviceIdentified);
        return redissonClient.getBucket(key, new SerializationCodec());
    }

    public String generatePlaintextString(SecurityUserDetails userDetails) {
        Map<String, Object> json = new LinkedHashMap<>();

        json.put(IdEntity.ID_FIELD_NAME, userDetails.getId());
        json.put(AuthenticationProperties.SECURITY_FORM_USERNAME_PARAM_NAME, userDetails.getUsername());
        json.put(PluginAuditEvent.TYPE_FIELD_NAME, userDetails.getType());
        json.put(NumberIdEntity.CREATION_TIME_FIELD_NAME, System.currentTimeMillis());

        if (userDetails instanceof MobileUserDetails) {
            MobileUserDetails mobileUserDetails = Casts.cast(userDetails);
            json.put(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, mobileUserDetails.getDeviceIdentified());
        }

        String plaintext = Casts.writeValueAsString(json);

        CipherService cipherService = cipherAlgorithmService.getCipherService(authenticationProperties.getAccessToken().getCipherAlgorithmName());
        byte[] key = Base64.decode(authenticationProperties.getAccessToken().getKey());
        ByteSource source = cipherService.encrypt(plaintext.getBytes(Charset.defaultCharset()), key);

        return source.getBase64();
    }

    public Map<String, Object> convertPlaintext(String plaintext) {
        return Casts.readValue(plaintext, new TypeReference<Map<String, Object>>() {});
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        SaveContextOnUpdateOrErrorResponseWrapper responseWrapper = WebUtils.getNativeResponse(response,
                SaveContextOnUpdateOrErrorResponseWrapper.class);
        if (Objects.nonNull(responseWrapper)) {
            super.saveContext(context, request, response);
        }

        saveRedissonSecurityContext(context, request, response);
    }

    /**
     * 删除缓存
     *
     * @param userDetails 移动端的用户明细实现
     */
    public void deleteContext(SecurityUserDetails userDetails) {
        RBucket<SecurityContext> bucket = getSecurityContextBucket(userDetails.getType(), userDetails.getId());
        bucket.deleteAsync();
    }

    /**
     * 删除缓存
     *
     * @param type 用户类型
     * @param id 设备唯一识别
     */
    public void deleteContext(String type, Object id) {
        RBucket<SecurityContext> bucket = getSecurityContextBucket(type, id);
        bucket.deleteAsync();
    }

    protected void saveRedissonSecurityContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        if (Objects.isNull(context.getAuthentication()) || !context.getAuthentication().isAuthenticated()) {
            return;
        }

        Authentication authentication = context.getAuthentication();

        if (authentication instanceof RememberMeAuthenticationToken) {
            RememberMeAuthenticationToken rememberMeToken = Casts.cast(authentication);
            if (rememberMeToken.isRememberMe()) {
                return;
            }
        }

        if (authentication instanceof SimpleAuthenticationToken) {
            SimpleAuthenticationToken simpleToken = Casts.cast(authentication);
            if (simpleToken.isRememberMe()) {
                return;
            }
        }

        Object details = authentication.getDetails();
        if (Objects.isNull(details) || !SecurityUserDetails.class.isAssignableFrom(details.getClass())) {
            return;
        }

        SecurityUserDetails userDetails = Casts.cast(details);
        if (MapUtils.isEmpty(userDetails.getMeta())) {
            return ;
        }

        String accessToken = userDetails
                .getMeta()
                .getOrDefault(AccessTokenProperties.DEFAULT_ACCESS_TOKEN_PARAM_NAME, StringUtils.EMPTY)
                .toString();

        if (StringUtils.isEmpty(accessToken)) {
            userDetails.getMeta().put(AccessTokenProperties.DEFAULT_ACCESS_TOKEN_PARAM_NAME, generatePlaintextString(userDetails));
        }

        RBucket<SecurityContext> bucket = getSecurityContextBucket(userDetails.getType(), userDetails.getId());
        bucket.set(context);
        TimeProperties time = authenticationProperties.getAuthenticationCache().getExpiresTime();

        if (Objects.nonNull(time)) {
            bucket.expireAsync(time.getDuration());
        }
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        boolean superValue = super.containsContext(request);
        SecurityContext context = readSecurityContextFromRequest(request);
        return superValue || Objects.nonNull(context);
    }

   /* @Override
    public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy strategy) {
        this.securityContextHolderStrategy = strategy;
    }*/

    /*static final class AccessTokenDeferredSecurityContext implements DeferredSecurityContext {

        private final List<Supplier<SecurityContext>> suppliers;
        private final SecurityContextHolderStrategy securityContextHolderStrategy;

        private SecurityContext securityContext;

        private boolean missingContext;

        public AccessTokenDeferredSecurityContext(List<Supplier<SecurityContext>> suppliers,
                                                  SecurityContextHolderStrategy securityContextHolderStrategy) {
            this.suppliers = suppliers;
            this.securityContextHolderStrategy = securityContextHolderStrategy;
        }

        @Override
        public boolean isGenerated() {
            init();
            return this.missingContext;
        }

        @Override
        public SecurityContext get() {
            init();
            return this.securityContext;
        }

        private void init() {
            if (this.securityContext != null) {
                return;
            }

            for (Supplier<SecurityContext> supplier : suppliers) {
                this.securityContext = supplier.get();
                if (Objects.nonNull(this.securityContext) && Objects.nonNull(this.securityContext.getAuthentication())) {
                    break;
                }
            }

            this.missingContext = (this.securityContext == null);
            if (this.missingContext) {
                this.securityContext = this.securityContextHolderStrategy.createEmptyContext();
            }
        }
    }*/
}
