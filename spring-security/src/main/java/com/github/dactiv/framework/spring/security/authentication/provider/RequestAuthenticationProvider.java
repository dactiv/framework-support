package com.github.dactiv.framework.spring.security.authentication.provider;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 请求认证供应者实现
 *
 * @author maurice.chen
 */
public class RequestAuthenticationProvider implements AuthenticationManager, AuthenticationProvider, MessageSourceAware, InitializingBean {

    /**
     * 国际化信息
     */
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    /**
     * 用户明细符合集合
     */
    private List<UserDetailsService> userDetailsServices;

    /**
     * redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 隐藏找不到用户异常，用登陆账户或密码错误异常
     */
    private boolean hideUserNotFoundExceptions = true;

    /**
     * 当前用户认证供应者实现
     *
     * @param userDetailsServices 账户认证的用户明细服务集合
     */
    public RequestAuthenticationProvider(RedissonClient redissonClient, List<UserDetailsService> userDetailsServices) {
        this.userDetailsServices = userDetailsServices;
        this.redissonClient = redissonClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 获取 token
        RequestAuthenticationToken token = Casts.cast(authentication);

        // 通过 token 获取对应 type 实现的 UserDetailsService
        Optional<UserDetailsService> optional = getUserDetailsService(token);

        String message = messages.getMessage(
                "PrincipalAuthenticationProvider.userDetailsServiceNotFound",
                "找不到适用于 " + token.getType() + " 的 UserDetailsService 实现"
        );

        // 获取实现类
        UserDetailsService userDetailsService = optional.orElseThrow(() -> new AuthenticationServiceException(message));
        // 开始授权，如果失败抛出异常
        SecurityUserDetails userDetails = doAuthenticate(token, userDetailsService);

        Collection<? extends GrantedAuthority> grantedAuthorities = userDetails.getAuthorities();
        // 获取认证缓存
        CacheProperties authorizationCache = userDetailsService.getAuthorizationCache(token);
        // 如果启用授权缓存，从授权缓存获取用户授权信息
        if (Objects.nonNull(authorizationCache) && CollectionUtils.isEmpty(grantedAuthorities)) {
            RList<GrantedAuthority> list = getAuthorizationList(token, userDetailsService);
            grantedAuthorities = list.range(0, list.size());
        }

        // 如果找不到授权信息，调用 UserDetailsService 的 getPrincipalAuthorities 方法获取当前用户授权信息
        if (CollectionUtils.isEmpty(grantedAuthorities)) {

            grantedAuthorities = userDetailsService.getPrincipalAuthorities(userDetails);
            if (Objects.nonNull(authorizationCache)) {

                RList<GrantedAuthority> list = getAuthorizationList(token, userDetailsService);
                list.addAllAsync(grantedAuthorities);

                if (Objects.nonNull(authorizationCache.getExpiresTime())) {
                    list.expireAsync(authorizationCache.getExpiresTime().getValue(), authorizationCache.getExpiresTime().getUnit());
                }
            }

        }

        PrincipalAuthenticationToken result = createSuccessAuthentication(userDetails, token, grantedAuthorities);
        userDetailsService.onSuccessAuthentication(result);

        return result;
    }

    protected SecurityUserDetails doAuthenticate(RequestAuthenticationToken token, UserDetailsService userDetailsService) {
        SecurityUserDetails userDetails = null;
        CacheProperties authenticationCache = userDetailsService.getAuthenticationCache(token);

        // 如果启用认证缓存，从认证缓存里获取用户
        if (Objects.nonNull(authenticationCache)) {
            RBucket<SecurityUserDetails> bucket = redissonClient.getBucket(authenticationCache.getName());
            userDetails = bucket.get();
        }

        //如果在缓存中找不到用户，调用 UserDetailsService 的 getAuthenticationUserDetails 方法获取当前用户
        if (Objects.isNull(userDetails)) {
            try {
                userDetails = userDetailsService.getAuthenticationUserDetails(token);
            } catch (AuthenticationException e) {
                // 如果 hideUserNotFoundExceptions true 并且是 UsernameNotFoundException 异常，抛出 用户名密码错误异常
                if (UsernameNotFoundException.class.isAssignableFrom(e.getClass()) && hideUserNotFoundExceptions) {
                    throw new BadCredentialsException(messages.getMessage(
                            "PrincipalAuthenticationProvider.badCredentials",
                            "用户名或密码错误"));
                } else {
                    throw e;
                }
            }
        }

        checkUserDetails(userDetails);

        String presentedPassword = token.getCredentials().toString();

        // 如果用户账户密码不正确，抛出用户名或密码错误异常
        if (!userDetailsService.matchesPassword(presentedPassword, token, userDetails)) {

            throw new BadCredentialsException(messages.getMessage(
                    "PrincipalAuthenticationProvider.badCredentials",
                    "用户名或密码错误"));
        }

        userDetails.setType(token.getType());
        // 如果启用认证缓存，存储用户信息到缓存里
        if (Objects.nonNull(authenticationCache)) {
            RBucket<SecurityUserDetails> bucket = redissonClient.getBucket(authenticationCache.getName());
            if (Objects.isNull(authenticationCache.getExpiresTime())) {
                bucket.set(userDetails);
            } else {
                bucket.set(
                        userDetails,
                        authenticationCache.getExpiresTime().getValue(),
                        authenticationCache.getExpiresTime().getUnit()
                );
            }
        }

        return userDetails;
    }

    /**
     * 获取授权集合
     *
     * @param token              token 值
     * @param userDetailsService 用户明细服务实现类
     *
     * @return redis 集合
     */
    public RList<GrantedAuthority> getAuthorizationList(RequestAuthenticationToken token, UserDetailsService userDetailsService) {
        CacheProperties authorizationCache = userDetailsService.getAuthorizationCache(token);

        return redissonClient.getList(authorizationCache.getName());
    }

    /**
     * 检查用户明细是否正确，如果错误，抛出 {@link AuthenticationException} 的异常
     *
     * @param userDetails 用户明细
     */
    protected void checkUserDetails(SecurityUserDetails userDetails) {

        // 如果获取不到用户，并且 hideUserNotFoundExceptions 等于 true 时，抛次用户名或密码错误异常，否则抛出找不到用户异常
        if (Objects.isNull(userDetails)) {
            if (hideUserNotFoundExceptions) {
                throw new BadCredentialsException(messages.getMessage(
                        "PrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            } else {
                throw new UsernameNotFoundException(messages.getMessage(
                        "PrincipalAuthenticationProvider.usernameNotFound",
                        "找不到用户信息"));
            }
        }

        // 如果用户被锁定，抛出用户被锁定异常
        if (!userDetails.isAccountNonLocked()) {

            throw new LockedException(messages.getMessage(
                    "PrincipalAuthenticationProvider.locked",
                    "用户已经被锁定"));
        }

        // 如果用户被禁用，抛出用户被禁用异常
        if (!userDetails.isEnabled()) {

            throw new DisabledException(messages.getMessage(
                    "PrincipalAuthenticationProvider.disabled",
                    "用户已被禁用"));
        }

        // 如果用户账户已过期，抛出用户账户已过期异常
        if (!userDetails.isAccountNonExpired()) {

            throw new AccountExpiredException(messages.getMessage(
                    "PrincipalAuthenticationProvider.expired",
                    "用户账户已过期"));
        }
    }

    /**
     * 创建认证信息
     *
     * @param userDetails        当前用户
     * @param token              当前认真 token
     * @param grantedAuthorities 当亲啊用户授权信息
     *
     * @return spring security 认证信息
     */
    protected PrincipalAuthenticationToken createSuccessAuthentication(SecurityUserDetails userDetails,
                                                                       PrincipalAuthenticationToken token,
                                                                       Collection<? extends GrantedAuthority> grantedAuthorities) {

        return new PrincipalAuthenticationToken(
                new UsernamePasswordAuthenticationToken(token.getPrincipal(), token.getCredentials()),
                token.getType(),
                userDetails,
                grantedAuthorities
        );
    }

    /**
     * 获取账户认证的用户明细服务
     *
     * @param token 当前用户认证 token
     *
     * @return 用户明细服务
     */
    public Optional<UserDetailsService> getUserDetailsService(PrincipalAuthenticationToken token) {
        return userDetailsServices.stream().filter(uds -> uds.getType().contains(token.getType())).findFirst();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return RequestAuthenticationToken.class.isAssignableFrom(authentication);
    }


    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(userDetailsServices, "至少要一个" + UserDetailsService.class.getName() + "接口的实现");
    }

    /**
     * 获取账户认证的用户明细服务集合
     *
     * @return 账户认证的用户明细服务集合
     */
    public List<UserDetailsService> getUserDetailsServices() {
        return userDetailsServices;
    }

    /**
     * 设置账户认证的用户明细服务集合
     *
     * @param userDetailsServices 账户认证的用户明细服务集合
     */
    public void setUserDetailsServices(List<UserDetailsService> userDetailsServices) {
        this.userDetailsServices = userDetailsServices;
    }

    /**
     * 是否隐藏找不到用户异常
     *
     * @return true 是，否则 false
     */
    public boolean isHideUserNotFoundExceptions() {
        return hideUserNotFoundExceptions;
    }

    /**
     * 设置是否隐藏找不到用户异常
     *
     * @param hideUserNotFoundExceptions true 是，否则 false
     */
    public void setHideUserNotFoundExceptions(boolean hideUserNotFoundExceptions) {
        this.hideUserNotFoundExceptions = hideUserNotFoundExceptions;
    }

}
