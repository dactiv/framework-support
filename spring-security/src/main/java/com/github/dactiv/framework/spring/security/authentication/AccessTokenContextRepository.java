package com.github.dactiv.framework.spring.security.authentication;

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
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 访问 token 上下文仓库实现，用于移动端用户明细登陆系统后，返回一个 token， 为无状态的 http 传输中，通过该 token 来完成认证授权等所有工作
 *
 * @author maurice.chen
 */
public class AccessTokenContextRepository extends HttpSessionSecurityContextRepository {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(AccessTokenContextRepository.class);

    private final RedissonClient redissonClient;

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private final AuthenticationProperties authenticationProperties;

    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    public AccessTokenContextRepository(RedissonClient redissonClient, AuthenticationProperties authenticationProperties) {
        this.redissonClient = redissonClient;
        this.authenticationProperties = authenticationProperties;
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

    @Override
    public DeferredSecurityContext loadDeferredContext(HttpServletRequest request) {
        DeferredSecurityContext superDeferredSecurityContext = super.loadDeferredContext(request);
        Supplier<SecurityContext> supplier = () -> readSecurityContextFromRequest(request);
        return new AccessTokenDeferredSecurityContext(List.of(superDeferredSecurityContext, supplier), securityContextHolderStrategy);
    }

    private SecurityContext readSecurityContextFromRequest(HttpServletRequest request) {
        String token = request.getHeader(authenticationProperties.getAccessToken().getAccessTokenHeaderName());
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter(authenticationProperties.getAccessToken().getAccessTokenParamName());
        }

        if (StringUtils.isEmpty(token)) {
            return null;
        }

        CipherService cipherService = cipherAlgorithmService.getCipherService(authenticationProperties.getAccessToken().getCipherAlgorithmName());
        byte[] key = Base64.decode(authenticationProperties.getAccessToken().getKey());

        try {
            ByteSource byteSource = cipherService.decrypt(Base64.decode(token), key);
            String plaintext = new String(byteSource.obtainBytes(), Charset.defaultCharset());

            MobileUserDetails plaintextUserDetail = convertPlaintext(plaintext);

            RBucket<SecurityContext> bucket = getSecurityContextBucket(plaintextUserDetail);
            SecurityContext context = bucket.get();

            MobileUserDetails userDetails = Casts.cast(context.getAuthentication().getDetails());
            String existToken = userDetails.getMeta().getOrDefault(AccessTokenProperties.DEFAULT_ACCESS_TOKEN_PARAM_NAME, StringUtils.EMPTY).toString();

            if (!StringUtils.equals(existToken, token)) {
                return null;
            }

            if (!StringUtils.equals(plaintextUserDetail.toUniqueValue(), userDetails.toUniqueValue())) {
                return null;
            }

            return context;

        } catch (CryptoException e) {
            LOGGER.error("通过密钥:" + authenticationProperties.getAccessToken().getKey() + "解密token:" + token + "失败", e);
        }

        return null;
    }

    public RBucket<SecurityContext> getSecurityContextBucket(MobileUserDetails mobileUserDetails) {
        String key = authenticationProperties.getAccessToken().getCache().getName(mobileUserDetails.getType() + CacheProperties.DEFAULT_SEPARATOR + mobileUserDetails.getDeviceIdentified());
        return redissonClient.getBucket(key, new SerializationCodec());
    }

    public String generatePlaintextString(MobileUserDetails mobileUserDetails) {
        Map<String, Object> map = Map.of(
                IdEntity.ID_FIELD_NAME, mobileUserDetails.getId(),
                AuthenticationProperties.SECURITY_FORM_PASSWORD_PARAM_NAME, mobileUserDetails.getPassword(),
                AuthenticationProperties.SECURITY_FORM_USERNAME_PARAM_NAME, mobileUserDetails.getUsername(),
                PluginAuditEvent.TYPE_FIELD_NAME, mobileUserDetails.getType(),
                DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, mobileUserDetails.getDeviceIdentified(),
                NumberIdEntity.CREATION_TIME_FIELD_NAME, System.currentTimeMillis()
        );

        String json = Casts.writeValueAsString(map);
        CipherService cipherService = cipherAlgorithmService.getCipherService(authenticationProperties.getAccessToken().getCipherAlgorithmName());
        byte[] key = Base64.decode(authenticationProperties.getAccessToken().getKey());
        ByteSource source = cipherService.encrypt(json.getBytes(Charset.defaultCharset()), key);

        return source.getBase64();
    }

    public MobileUserDetails convertPlaintext(String plaintext) {
        return Casts.readValue(plaintext, MobileUserDetails.class);
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        super.saveContext(context, request, response);

        saveRedissonSecurityContext(context);

    }

    /**
     * 删除缓存
     *
     * @param mobileUserDetails 移动端的用户明细实现
     */
    public void deleteContext(MobileUserDetails mobileUserDetails) {
        RBucket<SecurityContext> bucket = getSecurityContextBucket(mobileUserDetails);
        bucket.deleteAsync();
    }

    private void saveRedissonSecurityContext(SecurityContext context) {
        if (Objects.isNull(context.getAuthentication()) || !context.getAuthentication().isAuthenticated()) {
            return;
        }

        Object details = context.getAuthentication().getDetails();
        if (Objects.isNull(details) || !MobileUserDetails.class.isAssignableFrom(details.getClass())) {
            return;
        }

        MobileUserDetails mobileUserDetails = Casts.cast(details);
        String accessToken = mobileUserDetails
                .getMeta()
                .getOrDefault(AccessTokenProperties.DEFAULT_ACCESS_TOKEN_PARAM_NAME, StringUtils.EMPTY)
                .toString();

        if (StringUtils.isEmpty(accessToken)) {
            mobileUserDetails.getMeta().put(AccessTokenProperties.DEFAULT_ACCESS_TOKEN_PARAM_NAME, generatePlaintextString(mobileUserDetails));
        }

        RBucket<SecurityContext> bucket = getSecurityContextBucket(mobileUserDetails);

        TimeProperties time = authenticationProperties.getAuthenticationCache().getExpiresTime();

        if (Objects.isNull(time)) {
            bucket.set(context);
        } else {
            bucket.set(context, time.getValue(), time.getUnit());
        }
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        boolean superValue = super.containsContext(request);
        SecurityContext context = readSecurityContextFromRequest(request);
        return superValue || Objects.nonNull(context);
    }

    @Override
    public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy strategy) {
        this.securityContextHolderStrategy = strategy;
    }

    static final class AccessTokenDeferredSecurityContext implements DeferredSecurityContext {

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
    }
}
