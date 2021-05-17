package com.github.dactiv.framework.spring.security.authentication.provider;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;


/**
 * 匿名用户认证供应者
 *
 * @author maurice
 */
public class AnonymousUserAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private PasswordEncoder passwordEncoder;

    private final RedissonClient redissonClient;

    public AnonymousUserAuthenticationProvider(RedissonClient redissonClient) {
        setForcePrincipalAsString(true);
        this.redissonClient = redissonClient;
    }

    public void createUser(AnonymousUser user) {
        RBucket<User> bucket = redissonClient.getBucket(user.getUsername());

        if (bucket.isExists()) {
            bucket.setAsync(user);
        }
    }

    public void deleteUser(String username) {
        RBucket<User> bucket = redissonClient.getBucket(username);

        if (bucket.isExists()) {
            bucket.deleteAsync();
        }
    }

    public void updateUser(AnonymousUser user) {
        RBucket<User> bucket = redissonClient.getBucket(user.getUsername());
        Assert.isTrue(bucket.isExists(), "不存在[" + user.getUsername() + "]");
        bucket.set(user);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        if (authentication.getCredentials() == null) {

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "用户名密码错误"));
        }

        String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "用户名密码错误"));
        }

    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                principal,
                authentication.getCredentials(),
                user.getAuthorities()
        );

        result.setDetails(user);

        return result;
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        try {


            AnonymousUser loadedUser = null;

            Object value = getUserBucket(username).get();

            if (value != null) {
                loadedUser = Casts.cast(value);
            }

            if (loadedUser == null) {
                throw new BadCredentialsException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        "用户名密码错误"));
            }

            return new AnonymousUser(
                    loadedUser.getUsername(),
                    loadedUser.getPassword(),
                    loadedUser.getTempPassword(),
                    loadedUser.getAuthorities()
            );
        } catch (UsernameNotFoundException | InternalAuthenticationServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * 设置密码编码器
     *
     * @param passwordEncoder 密码编码器
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 获取用户缓存
     *
     * @param username
     *
     * @return
     */
    public RBucket<User> getUserBucket(String username) {
        return redissonClient.getBucket(UserDetailsService.DEFAULT_AUTHENTICATION_KEY_NAME + username);
    }

}
